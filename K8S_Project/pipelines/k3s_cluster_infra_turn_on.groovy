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

        stage('Install prerequieries JQ, kubectl and Ansible') {
            steps {
                sh '''
                curl -fsSL https://pkgs.k8s.io/core:/stable:/v1.28/deb/Release.key | sudo gpg --yes --dearmor -o /etc/apt/keyrings/kubernetes-apt-keyring.gpg
                echo 'deb [signed-by=/etc/apt/keyrings/kubernetes-apt-keyring.gpg] https://pkgs.k8s.io/core:/stable:/v1.28/deb/ /' | sudo tee /etc/apt/sources.list.d/kubernetes.list
                sudo apt-get update
                sudo apt-get install -y jq kubeadm kubelet kubectl
                sudo apt-get install -y ansible
                sudo apt install awscli -y
                 '''
            }
        }
        stage('Main Initialize and Plan') {
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
                        k3s_master_instance_public_dns=$(terraform output -raw k3s_master_instance_public_dns)
                        # Set it as an environment variable
                        export k3s_master_instance_public_dns="$k3s_master_instance_public_dns"
                        # Optional: Print the environment variable
                        echo "The instance public DNS is set to: $k3s_master_instance_public_dns"                        
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
                    ansible-playbook -i control_plane_hosts master-setup.yaml
                    ansible-playbook -i worker_hosts worker-setup.yaml
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
        stage('deploy Pacman') {
            steps {
                script {
                    // Trigger another Jenkins pipeline with pacman
                    build job: 'deploy_pacman_in_to_k3s_cluster',
                    wait: true
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