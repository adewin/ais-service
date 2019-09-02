module vpc {
  source = "terraform-aws-modules/vpc/aws"

  name = "ukho-network"
  cidr = "10.1.0.0/16"

  azs             = ["eu-west-2a", "eu-west-2b", "eu-west-2c"]
  private_subnets = ["10.1.1.0/24"]
}
