output network_security_group_id {
  value = aws_security_group.security_group.id
}

output network_subnet_ids {
  value = module.vpc.private_subnets
}
