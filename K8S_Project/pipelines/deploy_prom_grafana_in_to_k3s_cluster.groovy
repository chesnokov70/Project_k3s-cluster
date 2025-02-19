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
        NAMESPACE = "monitoring" 
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
                            curl -fsSL https://raw.githubusercontent.com/helm/helm/main/scripts/get-helm-3 | bash -s
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
                        kubectl get namespace $NAMESPACE || kubectl create namespace $NAMESPACE
                    '''
                }
            }
        }


        stage('Deploy Prometheus') {
            steps {
                script {
                    sh '''
                        helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
                        helm repo update
                        helm upgrade --install prometheus prometheus-community/kube-prometheus-stack --namespace $NAMESPACE --wait
                    '''
                }
            }
        }

        stage('Deploy Grafana') {
            steps {
                script {
                    sh '''
                        helm repo add grafana https://grafana.github.io/helm-charts
                        helm repo update
                        helm upgrade --install grafana grafana/grafana --namespace $NAMESPACE --set adminPassword=admin --wait
                    '''
                }
            }
        }

        stage('Verify Deployment') {
            steps {
                script {
                    sh '''
                        echo "Checking running pods in the monitoring namespace..."
                        kubectl get pods -n $NAMESPACE
                    '''
                }
            }
        }
        
    }

    post {  // This block must be inside `pipeline {}` but outside `stages {}`
        success {
            echo '✅ Prometheus and Grafana have been successfully deployed!'
        }
        failure {
            echo '❌ Deployment failed. Check logs for details.'
        }
        always {
            cleanWs()
        }
    }
}
