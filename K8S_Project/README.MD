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
#-------------------------------master setup--------------------------------------
###----------------------------update packages
```
sudo apt update
```

###------------------------------ install extra packages/updates------------------
```
sudo apt update && sudo apt install apt-transport-https ca-certificates curl software-properties-common -y
```
###--------------------------------launch master------------------------------------
```
curl -sfL https://get.k3s.io | INSTALL_K3S_EXEC="server --disable traefik --disable servicelb --tls-san 3.235.233.83" sh -s - --token u2Qw5PbXC887MMv85LeGSergeiChes
```

###--------------------------------copy ssh key--------------------------------------
```

scp -i ~/.ssh/ssh_instance_key.pem ~/.ssh/ssh_instance_key.pem ubuntu@54.89.85.221:/home/ubuntu/.ssh/
```

###---------------------------------connect to the worker x2
```
ssh ubuntu@10.0.2.37 -i ~/.ssh/jenkins-ansible.pem 
```
###----------------------------------worker setup

###----------------------------------connect to the worker----------------------------
```
ssh ubuntu@10.0.2.37 -i ~/.ssh/jenkins-ansible.pem 
```
###-----------------------------------update apt
```
sudo apt update
```
###-------------------------------------install updated apps

```
sudo apt install apt-transport-https ca-certificates curl software-properties-common 
```

###------------------------------------Launch Worker agent---------------------------------###

```
curl -sfL https://get.k3s.io | sh -s - agent --server https://10.0.1.53:6443 --token u2Qw5PbXC887MMv85LeGSergeiChes --kubelet-arg=cloud-provider=external
```

#--------------------------------------------------------
run
main_setup
control_plane_nodes
worker_setupcd

kubectl label node ip-10-0-2-12 node-role.kubernetes.io/worker=worker
kubectl label node ip-10-0-2-21 node-role.kubernetes.io/worker=worker 

aws ssm get-parameter --name "/K3S_project/kubeconfig" --with-decryption --query "Parameter.Value" --output text > ~/.kube/config

kubectl port-forward -n monitoring svc/prometheus-grafana 3000:80

export KUBECONFIG=k3s_test.yaml

1. Установка Helm (если не установлен)
Helm — это пакетный менеджер для Kubernetes.
Устанавливаем Helm:
curl https://raw.githubusercontent.com/helm/helm/main/scripts/get-helm-3 | bash
Проверяем установку:
helm version
2. Установка Prometheus и Grafana с помощью Helm
2.1 Добавляем репозиторий Helm-чартов
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
helm repo update
2.2 Создаем пространство имен
kubectl create namespace monitoring
2.3 Устанавливаем Prometheus Stack
helm upgrade --install prometheus prometheus-community/kube-prometheus-stack -n monitoring --create-namespace
Проверяем установку:
kubectl get pods -n monitoring
Вывод должен быть примерно таким:
NAME                                                    READY   STATUS    RESTARTS   AGE
alertmanager-prometheus-kube-prometheus-alertmanager-0   2/2     Running   0          2m
prometheus-kube-prometheus-operator-7c7b6dd8b9-xyz      1/1     Running   0          2m
prometheus-kube-state-metrics-78c7d5bd88-xyz            1/1     Running   0          2m
prometheus-kube-prometheus-prometheus-0                 2/2     Running   0          2m
prometheus-grafana-xyz                                  2/2     Running   0          2m
3. Доступ к Grafana и Prometheus
3.1 Доступ к Grafana (порт 3000)
Вы можете пробросить порт:
kubectl port-forward -n monitoring svc/prometheus-grafana 3000:80
Теперь можно открыть Grafana в браузере:
:point_right: http://localhost:3000
Логин/пароль по умолчанию:
Логин: admin
Пароль: prom-operator


Vitali Mishyn
  7:44 PM
4. Доступ к Prometheus (порт 9090)
kubectl port-forward -n monitoring svc/prometheus-kube-prometheus-prometheus 9090:9090
Теперь можно открыть Prometheus в браузере:
:point_right: http://localhost:9090
5. Настройка Grafana (Добавление Prometheus как источника данных)
Заходим в Grafana: http://localhost:3000
Переходим в "Configuration" → "Data Sources".  
http://localhost:3000/datasources
Нажимаем "Add data source".
Выбираем "Prometheus".
В поле URL вводим:
http://prometheus-kube-prometheus-prometheus.monitoring.svc.cluster.local:9090
6.Нажимаем "Save & Test". Должно появиться сообщение "Data source is working".
5. Настройка Alertmanager (по желанию)
alertmanager:
  enabled: true
  config:
    global:
      resolve_timeout: 5m
    route:
      receiver: "slack"
    receivers:
      - name: "slack"
        slack_configs:
          - channel: "#alerts"
            send_resolved: true
            api_url: "https://hooks.slack.com/services/YOUR/SLACK/WEBHOOK"
Применяем с кастомными параметрами:
helm upgrade --install prometheus prometheus-community/kube-prometheus-stack -n monitoring -f values.yaml (edited) 

Reset Grafana Admin Password
# Access the Grafana Pod:
kubectl exec -n monitoring -it $(kubectl get pod -n monitoring -l app.kubernetes.io/name=grafana -o jsonpath="{.items[0].metadata.name}") -- sh
# Run the Reset Command:
grafana-cli admin reset-admin-password NEW_PASSWORD
# Exit the Pod:
exit