
variable "k3s_cluster_token" {
  description = "Shared token for K3s cluster node registration"
  type        = string
  default     = "u2Qw5PbXC887MMv85LeGSergeiChes" # Replace with your actual token
}

variable "region" {
  description = "Please Enter AWS Region to deploy Server"
  type        = string
  default     = "us-east-1"
}
