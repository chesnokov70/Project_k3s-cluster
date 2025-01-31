terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~>5"
    }
    random = {
      source  = "hashicorp/random"
      version = "~>3.5"
    }
  }
  backend "s3" {
    bucket = "terraform-state-s3-sergei"
    key    = "k3s_main_vpc/infra_setup.tfstate"
    region = "us-east-1"
  }
  required_version = ">= 1.3"
}

provider "aws" {
  region = var.region
}
