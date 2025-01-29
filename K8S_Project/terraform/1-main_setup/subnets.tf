
# Create Public Subnets in all AZs
resource "aws_subnet" "k3s_public_subnet" {
  count                   = length(data.aws_availability_zones.available.names) # example [az1, az2, az3] => 5 subnets
  vpc_id                  = aws_vpc.k3s_vpc.id
  cidr_block              = cidrsubnet(var.public_subnet_cidr, 4, count.index) # example 10.0.0.0/24
  map_public_ip_on_launch = true
  availability_zone       = data.aws_availability_zones.available.names[count.index]

  tags = {
    Name = format("K3s_Public_Subnet_%s", data.aws_availability_zones.available.names[count.index])
  }
}

# Create Private Subnets in all AZs
resource "aws_subnet" "k3s_private_subnet" {
  count = length(data.aws_availability_zones.available.names)

  vpc_id            = aws_vpc.k3s_vpc.id
  cidr_block        = cidrsubnet(var.private_subnet_cidr, 4, count.index)
  availability_zone = data.aws_availability_zones.available.names[count.index]

  tags = {
    Name = format("K3s_Private_Subnet_%s", data.aws_availability_zones.available.names[count.index])
  }
}

# cidrsubnet // explain 
# The cidrsubnet function is used to calculate subnets within a given CIDR block.
# The function takes three arguments: an IP address, a prefix length, and a subnet number.
# The function returns the subnet address with the given prefix length.
# The subnet number is used to calculate the subnet address.
# The subnet number is a zero-based index of the subnets within the given CIDR block.