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
        CREDENTIALS_ID = 'ssh_github_access_key' // Replace with your credential ID in Jenkins
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

        stage('update pacman url'){
            steps {
                dir('K8S_Project/cluster_applications/pacman/') {
                    script {
                        sh '''
                        chmod +x ./update_host_pacman.sh
                        ./update_host_pacman.sh
                        cat 2-pacman.yaml
                        '''
                    }
                }
            }
        }

        stage('Install ingress') {
            steps {
                dir('K8S_Project/ingress_setup_cluster/'){
                    script {
                        sh '''
                        kubectl get nodes
                        kubectl apply -f 1-metal_lb.yaml
                        kubectl apply -f 2-nginx-ingress.yaml
                        sleep 30  
                        '''
                    }
                }
            }
        }
        stage('Deploy pacman') {
            steps {
                dir('K8S_Project/cluster_applications/pacman') {
                    script {
                        sh '''
                        kubectl apply -f 1-mongo-db.yaml
                        kubectl apply -f 2-pacman.yaml
                        sleep 30
                        kubectl get pods -A
                        '''
                    }
                }
            }
        }
    }
    post {
        always {
            cleanWs()
        }
    }
}