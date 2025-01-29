# Define the VPC
resource "aws_vpc" "k3s_vpc" {
  cidr_block           = "10.0.0.0/16" # 255.255.0.0 = 65,536 IPs (256 * 256) 
  enable_dns_hostnames = true

  tags = {
    Name = "K3s_VPC"
  }
}

# Define a Public Subnet
# resource "aws_subnet" "k3s_public_subnet" {
#  vpc_id                  = aws_vpc.k3s_vpc.id
#  cidr_block              = "10.0.1.0/24" # 255.255.255.0 = 256 IPs
#  map_public_ip_on_launch = true
#  availability_zone = data.aws_availability_zones.available.names[0]
#  tags = {
#    Name = "K3s_Public_Subnet"
#  }
# }

# Define a Private Subnet
# resource "aws_subnet" "k3s_private_subnet" {
#  vpc_id     = aws_vpc.k3s_vpc.id
#  cidr_block = "10.0.2.0/24" # 255.255.255.0 = 256 IPs
#  availability_zone = data.aws_availability_zones.available.names[1]
#  tags = {
#    Name = "K3s_Private_Subnet"
#  }
# }

# Create a VPC Peering Connection
resource "aws_vpc_peering_connection" "k3s_vpc_peering" {
  peer_vpc_id = data.aws_vpc.default.id
  vpc_id      = aws_vpc.k3s_vpc.id
  auto_accept = true

  tags = {
    Name = "K3s_VPC_Peering"
  }
}

# add more configs here
