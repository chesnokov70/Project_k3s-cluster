output "k3s_master_instance_private_ip" {
    value = data.aws_instances.asg_instances.private_ips[0]
}
output "k3s_master_instance_public_ip" {
    value = data.aws_instances.asg_instances.public_ips[0]
}

output "k3s_master_instance_public_dns" {
  value = data.aws_instance.k3s_master_instance_public_dns.public_dns
}

resource "local_file" "ansible-hosts" {
  filename = "../../ansible/control_plane_hosts"
  content = templatefile("./templates/ansible-hosts.tftpl", {
    public-ip   = data.aws_instances.asg_instances.public_ips[0]
  })
}

resource "local_file" "ansible-hosts-ec2" {
  filename = "../../ansible/control_plane_ip"
  content = data.aws_instances.asg_instances.public_ips[0]
}