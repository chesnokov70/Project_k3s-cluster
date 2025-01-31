# Variable definitions for CIDR blocks
variable "public_subnet_cidr" {
  description = "The CIDR block for the VPC"
  default     = "10.0.1.0/24" # 256 hosts
}

variable "private_subnet_cidr" {
  description = "The CIDR block for the VPC"
  default     = "10.0.2.0/24" # 256 hosts
}

variable "allow_ports" {
  description = "List of Ports to open for server"
  type        = list
  default     = ["22", "80", "443", "22", "6443", "8080", "10250"]
}

variable "region" {
  description = "Please Enter AWS Region to deploy Server"
  type        = string
  default     = "us-east-1"
}
