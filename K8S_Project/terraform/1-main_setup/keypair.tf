
resource "tls_private_key" "ec2-keypair" {
  algorithm = "ED25519"
}
resource "local_file" "private_key_pem" {
  content  = tls_private_key.ec2-keypair.private_key_openssh
  filename = "../../ansible/k3s-keypair.pem"

  provisioner "local-exec" {
    command = "chmod 400 ${local_file.private_key_pem.filename}"
  }
}
resource "aws_key_pair" "ec2-keypair" {
  key_name   = "k3s-keypair"
  public_key = tls_private_key.ec2-keypair.public_key_openssh
}

resource "aws_ssm_parameter" "private_key_param" {
  name        = "/K3S_project/k3s-keypair.pem"
  type        = "SecureString"
  value       = local_file.private_key_pem.content
}
