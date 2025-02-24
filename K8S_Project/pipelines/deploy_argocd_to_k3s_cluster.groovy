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

        // stage('Retrieve ArgoCD Admin Password') {
        //     steps {
        //         script {
        //             def argocdPassword = sh(
        //                 script: "kubectl get secret argocd-initial-admin-secret -n ${env.NAMESPACE} -o jsonpath='{.data.password}' | base64 --decode",
        //                 returnStdout: true
        //             ).trim()
        //             echo "🔑 ArgoCD Admin Password: ${argocdPassword}"
        //         }
        //     }
        // }     

        #---------------------------------
        stage('Retrieve ArgoCD Admin Password') {
            steps {
                script {
                    def maxRetries = 5
                    def attempt = 0
                    def secretExists = ""

                    while (attempt < maxRetries) {
                        secretExists = sh(
                            script: "kubectl get secret argocd-initial-admin-secret -n ${env.NAMESPACE} --ignore-not-found",
                            returnStdout: true
                        ).trim()

                        if (secretExists) {
                            break
                        }

                        echo "🔄 Waiting for ArgoCD admin secret... Attempt ${attempt + 1}/${maxRetries}"
                        sh 'sleep 5'  // Wait 5 seconds before retrying
                        attempt++
                    }

                    if (secretExists) {
                        def argocdPassword = sh(
                            script: "kubectl get secret argocd-initial-admin-secret -n ${env.NAMESPACE} -o jsonpath='{.data.password}' | base64 --decode",
                            returnStdout: true
                        ).trim()
                        echo "🔑 ArgoCD Admin Password: ${argocdPassword}"
                    } else {
                        echo "⚠️ ArgoCD Admin Secret not found! Resetting password..."
                        sh '''
                            kubectl -n argocd patch secret argocd-secret -p '{"stringData": {"admin.password": "$2a$10$uW8.YhNkhzrQ/XVy5lfnpeItQZp9czOrRei5kfjhxu9Dj4jlE52uS", "admin.passwordMtime": "'$(date +%FT%T%Z)'"}}'
                        '''
                        echo "✅ Password reset to 'admin'."
                    }
                }
            }
        }
            
        #---------------------------------   

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