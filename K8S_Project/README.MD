### K3S Project documentation https://docs.k3s.io/ ###


 how to get pods in the new cluster
kubectl get pods -A --insecure-skip-tls-verify=true

export KUBECONFIG=~/.kube/cluster_test_config

example of the config file 

```
apiVersion: v1
clusters:
- cluster:
    certificate-authority-data: "uSEyoURceRTIFICATE"
    server: https://${uSE_yOUR_ip}:6443
  name: test_k3s_aws
contexts:
- context:
    cluster: test_k3s_aws
    user: test_k3s_aws
  name: test_k3s_aws
current-context: test_k3s_aws
kind: Config
preferences: {}
users:
- name: test_k3s_aws
  user:
    client-certificate-data: "UseYourCert"
    client-key-data: "useYourData"
```

```
master setup
###
update packages
```
sudo apt update
```

### install extra packages/updates
```
sudo apt install apt-transport-https ca-certificates curl software-properties-common 
```
### launch master
```
curl -sfL https://get.k3s.io | INSTALL_K3S_EXEC="server --disable traefik --disable servicelb --tls-san 3.235.233.83" sh -s - --token u2Qw5PbXC887MMv85LeG
```

### copy ssh key
```
scp -i ~/Downloads/ssh-keys/jenkins-ansible.pem  /home/axelp/Downloads/ssh-keys/jenkins-ansible.pem ubuntu@3.235.233.83:/home/ubuntu/.ssh
```

### connect to the worker x2
```
ssh ubuntu@10.0.2.37 -i ~/.ssh/jenkins-ansible.pem 
```
### worker setup

### connect to the worker###
```
ssh ubuntu@10.0.2.37 -i ~/.ssh/jenkins-ansible.pem 
```
### update apt
```
sudo apt update
```
### install updated apps

```
sudo apt install apt-transport-https ca-certificates curl software-properties-common 
```

###Launch Worker agent###

```
curl -sfL https://get.k3s.io | sh -s - agent --server https://10.0.1.53:6443 --token u2Qw5PbXC887MMv85LeG --kubelet-arg=cloud-provider=external
```

#--------------------------------------------------------
run
main_setup
control_plane_nodes
