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

        stage('Install prerequieries JQ, kubectl and Ansible') {
            steps {
                sh '''
                curl -fsSL https://pkgs.k8s.io/core:/stable:/v1.28/deb/Release.key | sudo gpg --yes --dearmor -o /etc/apt/keyrings/kubernetes-apt-keyring.gpg
                echo 'deb [signed-by=/etc/apt/keyrings/kubernetes-apt-keyring.gpg] https://pkgs.k8s.io/core:/stable:/v1.28/deb/ /' | sudo tee /etc/apt/sources.list.d/kubernetes.list
                sudo apt-get update
                sudo apt-get install -y kubeadm kubelet kubectl
                sudo apt-get install -y ansible
                sudo apt install awscli -y
                 '''
            }
        }        

        stage('Initialize and Plan') {
            steps {
                dir('K8S_Project/terraform/1-main_setup') {
                    sh '''
                        terraform init
                        terraform plan -out=tfplan
                    '''
                }
            }
        }

        stage('Main Apply Infrastructure') {
            steps {
                dir('K8S_Project/terraform/1-main_setup') {
                    sh '''
                        terraform apply -input=false "tfplan"
                    '''
                }
            }
        }
        stage('Control Plane Nodes Initialize and Plan') {
            steps {
                dir('K8S_Project/terraform/2-control_plane_nodes') {
                    sh '''
                        terraform init
                        terraform plan -out=tfplan
                    '''
                }
            }
        }
        stage('Control Plane Nodes Apply Infrastructure') {
            steps {
                dir('K8S_Project/terraform/2-control_plane_nodes') {
                    sh '''
                        terraform apply -input=false "tfplan"
                    '''
                }
            }
        }
        stage('Workers Initialize and Plan') {
            steps {
                dir('K8S_Project/terraform/3-worker_setup') {
                    sh '''
                        terraform init
                        terraform plan -out=tfplan
                    '''
                }
            }
        }
        stage('Workers Nodes Apply Infrastructure') {
            steps {
                dir('K8S_Project/terraform/3-worker_setup') {
                    sh '''
                        terraform apply -input=false "tfplan"
                    '''
                }
            }
        }
        stage('Setup Control Plane Ansible') {
            steps {
                dir('K8S_Project/ansible/') {
                    sh '''
                    ansible-playbook master-setup.yaml
                    ansible-playbook worker-setup.yaml
                    '''
                }
            }
        }
        stage('Upload konfig to Parameter Store') {
            steps {
                dir('K8S_Project/ansible/') {
                    script {
                        sh '''
                            aws ssm put-parameter --name "/K3S_project/kubeconfig" --value "$(cat kubeconfig)" --type "SecureString" --overwrite --region "us-east-1"
                            echo "Command to download kubeconfig:"
                            echo 'aws ssm get-parameter --name "/K3S_project/kubeconfig" --with-decryption --query "Parameter.Value" --output text > config'
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
