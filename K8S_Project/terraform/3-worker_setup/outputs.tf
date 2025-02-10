output "k3s_workers_instance_private_ip" {
  value = data.aws_instances.asg_instances.private_ips
}

resource "local_file" "ansible-hosts" {
  filename = "../../ansible/worker_hosts"
  content = templatefile("./templates/ansible-hosts.tftpl", {
    private-ips   = join("\n", data.aws_instances.asg_instances.private_ips) })
}