resource "aws_launch_template" "k3s_master" {
  name_prefix   = "k3s-master-"
  image_id      = data.aws_ami.ubuntu_ami.id # Ubuntu amd64 (x86_64)
  instance_type = var.instance_type            # Update as necessary
  key_name      = "ssh_instance_key"       # Update with your SSH key name
  # default_version = 1 suppose to be used to set launch tempolate to latest version


  vpc_security_group_ids = [data.aws_security_group.k3s_sg.id]
  # user_data = base64encode(<<EOF
  #     #!/bin/bash
  #     # Install K3s server with predefined token
  #     curl -sfL https://get.k3s.io | sh -s - server --token u2Qw5PbXC887MMv85LeG
  #     EOF
  # )

  iam_instance_profile {
    name = data.aws_iam_instance_profile.k3s_node_instance_profile.name
  }

  tag_specifications {
    resource_type = "instance"
    tags = {
      Name = "K3s_Master"
    }
  }
}


#    command = "scp -i ~/.ssh/ssh_instance_key.pem ~/.ssh/ssh_instance_key.pem ubuntu@${data.aws_instances.asg_instances.public_ips[0]}:/home/ubuntu/.ssh/"


  #----------------------------------------------
 