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
        KUBECONFIG = "/var/jenkins_home/.kube/config"    
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
                            sparseCheckoutPaths: [[path: 'K8S_Project/terraform/']]
                        ]],
                        userRemoteConfigs: [[
                            url: env.GIT_REPO_URL,
                            credentialsId: env.CREDENTIALS_ID
                        ]]
                    ])
                }
            }
        }

        stage('Workers Initialize') {
            steps {
                dir('K8S_Project/terraform/3-worker_setup') {
                    sh '''
                        terraform init
                    '''
                }
            }
        }
        
        stage('Workers Destroy Plan') {
            steps {
                dir('K8S_Project/terraform/3-worker_setup') {
                    sh '''
                        terraform plan -destroy -out=destroy-tfplan
                    '''
                }
            }
        }

        stage('Workers Destroy Apply') {
            steps {
                dir('K8S_Project/terraform/3-worker_setup') {
                    sh '''
                        terraform apply -input=false "destroy-tfplan"
                    '''
                }
            }
        }
        stage('Control Plane Initialize') {
            steps {
                dir('K8S_Project/terraform/2-control_plane_nodes') {
                    sh '''
                        terraform init
                    '''
                }
            }
        }
         stage('Control Plane Destroy Plan') {
            steps {
                dir('K8S_Project/terraform/2-control_plane_nodes') {
                    sh '''
                        terraform plan -destroy -out=destroy-tfplan
                    '''
                }
            }
        }

        stage('Control Plane Destroy Apply') {
            steps {
                dir('K8S_Project/terraform/2-control_plane_nodes') {
                    sh '''
                        terraform apply -input=false "destroy-tfplan"
                    '''
                }
            }
        }

        stage('Main Initialize') {
            steps {
                dir('K8S_Project/terraform/1-main_setup') {
                    sh '''
                        terraform init
                    '''
                }
            }
        } 

        stage('Destroy Plan') {
            steps {
                dir('K8S_Project/terraform/1-main_setup') {
                    sh '''
                        terraform plan -destroy -out=destroy-tfplan
                    '''
                }
            }
        }

        stage('Destroy Apply') {
            steps {
                dir('K8S_Project/terraform/1-main_setup') {
                    sh '''
                        terraform apply -input=false "destroy-tfplan"
                    '''
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
