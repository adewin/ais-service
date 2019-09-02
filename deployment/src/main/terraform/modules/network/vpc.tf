module vpc {
  source = "terraform-aws-modules/vpc/aws"

  create_vpc = false

  manage_default_vpc               = true
  default_vpc_name                 = "default"
  default_vpc_enable_dns_hostnames = true
}
