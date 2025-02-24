pipeline {
    options {
        ansiColor('xterm')
    }

    agent any

    tools {
        terraform 'tf1.6'
    }

    environment {
        GIT_REPO_URL = 'git@github.com:chesnokov70/Project_k3s-cluster.git'
        CREDENTIALS_ID = 'ssh_github_access_key'
        KUBECONFIG = "/var/jenkins_home/.kube/config"   
        NAMESPACE = "argocd" 
    }

    stages {
        stage('Sparse Checkout') {
            steps {
                script {
                    checkout([
                        $class: 'GitSCM',
                        branches: [[name: 'main']],
                        doGenerateSubmoduleConfigurations: false,
                        extensions: [[
                            $class: 'SparseCheckoutPaths',
                            sparseCheckoutPaths: [[path: 'K8S_Project/']]
                        ]],
                        userRemoteConfigs: [[
                            url: env.GIT_REPO_URL,
                            credentialsId: env.CREDENTIALS_ID
                        ]]
                    ])
                }
            }
        }

        stage('Set Up Kubernetes Config') {
            steps {
                script {
                    sh '''
                        mkdir -p ~/.kube
                        aws ssm get-parameter --name "/K3S_project/kubeconfig" --with-decryption --query "Parameter.Value" --output text > ~/.kube/config
                        export KUBECONFIG=$KUBECONFIG
                    '''
                }
            }
        }

        stage('Install Helm') {
            steps {
                script {
                    sh '''
                        if ! command -v helm &> /dev/null
                        then
                            echo "Helm not found, installing..."
                            curl https://raw.githubusercontent.com/helm/helm/main/scripts/get-helm-3 | bash
                        else
                            echo "Helm is already installed."
                        fi
                        helm version
                    '''
                }
            }
        }

        stage('Create Namespace') {
            steps {
                script {
                    sh '''
                        if ! kubectl get namespace $NAMESPACE; then
                            kubectl create namespace $NAMESPACE
                        else
                            echo "Namespace $NAMESPACE already exists."
                        fi
                    '''
                }
            }
        }

        stage('Deploy ArgoCD') {
            steps {
                script {
                    sh '''
                        helm repo add argo https://argoproj.github.io/argo-helm
                        helm repo update
                        helm upgrade --install argocd argo/argo-cd --namespace $NAMESPACE --create-namespace
                    '''
                }
            }
        }

        stage('Verify ArgoCD Deployment') {
            steps {
                script {
                    sh '''
                        echo "Checking running pods in the $NAMESPACE namespace..."
                        kubectl get pods -n $NAMESPACE
                    '''
                }
            }
        }
    }

    post {
        success {
            echo 'ArgoCD has been successfully deployed!'
        }
        failure {
            echo 'Deployment failed. Check logs for details.'
        }
        always {
            cleanWs()
        }
    }
}