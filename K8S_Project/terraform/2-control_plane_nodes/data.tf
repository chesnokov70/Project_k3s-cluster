data "aws_vpc" "k3s_vpc" {
  tags = {
    Name = "K3s_VPC"
  }
}

data "aws_security_group" "k3s_sg" {
  name = "k3s-sg"
}

data "aws_subnets" "k3s_public_subnets" {
  filter {
    name   = "vpc-id"
    values = [data.aws_vpc.k3s_vpc.id]
  }
  filter {
    name   = "tag:Name"
    values = ["K3s_Public_Subnet*"]
  }
}

data "aws_instances" "asg_instances" {
  depends_on = [aws_autoscaling_group.k3s_master_asg]
  instance_tags = {
    "aws:autoscaling:groupName" = aws_autoscaling_group.k3s_master_asg.name
  }
}

data "aws_iam_instance_profile" "k3s_node_instance_profile" {
  name = "k3s_node_instance_profile"
}

data "aws_instance" "k3s_master_instance_public_dns" {
  depends_on = [aws_autoscaling_group.k3s_master_asg]
  instance_id = data.aws_instances.asg_instances.ids[0]
}

data "aws_ami" "ubuntu_ami" {
  most_recent = true
  
  filter {
    name   = "name"
    values = ["ubuntu/images/hvm-ssd-gp3/ubuntu-noble-24.04-amd64-server-*"]
  }
  
  filter {
    name = "architecture"
    values = ["x86_64"]
  }

  filter {
    name   = "owner-id"
    values = ["099720109477"] # Canonical's AWS account ID
  }

  filter {
    name   = "state"
    values = ["available"]
  }

}