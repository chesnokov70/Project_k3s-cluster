resource "aws_security_group" "k3s_sg" {
  name        = "k3s-sg"
  description = "Security group for K3s cluster"
  vpc_id      = aws_vpc.k3s_vpc.id

  dynamic "ingress" {
    for_each = {
      "ssh"          = { from_port = 22, to_port = 22, protocol = "tcp", cidr = "0.0.0.0/0" }
      "http"         = { from_port = 80, to_port = 80, protocol = "tcp", cidr = "0.0.0.0/0" }
      "https"        = { from_port = 443, to_port = 443, protocol = "tcp", cidr = "0.0.0.0/0" }
      "k8s_api"      = { from_port = 6443, to_port = 6443, protocol = "tcp", cidr = "0.0.0.0/0" }
      "echo_request" = { from_port = 8, to_port = 0, protocol = "icmp", cidr = "0.0.0.0/0" }
      "api_server"   = { from_port = 6443, to_port = 6443, protocol = "tcp", cidr = aws_vpc.k3s_vpc.cidr_block }
      "kubelet"      = { from_port = 10250, to_port = 10250, protocol = "tcp", cidr = aws_vpc.k3s_vpc.cidr_block }
      "etcd"         = { from_port = 2379, to_port = 2380, protocol = "tcp", cidr = aws_vpc.k3s_vpc.cidr_block }
      "nodeport"     = { from_port = 30000, to_port = 32767, protocol = "tcp", cidr = aws_vpc.k3s_vpc.cidr_block }
      "flannel"      = { from_port = 6783, to_port = 6784, protocol = "tcp", cidr = aws_vpc.k3s_vpc.cidr_block }
      "flannel_udp"  = { from_port = 8285, to_port = 8285, protocol = "udp", cidr = aws_vpc.k3s_vpc.cidr_block }
      "flannel_udp2"  = { from_port = 8472, to_port = 8472, protocol = "udp", cidr = aws_vpc.k3s_vpc.cidr_block }
    }

    content {
      from_port   = ingress.value.from_port
      to_port     = ingress.value.to_port
      protocol    = ingress.value.protocol
      cidr_blocks = [ingress.value.cidr]
      description = "Allow ${ingress.key}"
    }
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
    description = "Allow all outbound traffic"
  }
}