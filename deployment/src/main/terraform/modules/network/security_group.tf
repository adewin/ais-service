resource aws_security_group security_group {
  name   = "aws_security_group"
  vpc_id = module.vpc.vpc_id
}
