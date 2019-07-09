resource aws_s3_bucket jobs_bucket {
  bucket = var.jobs_bucket
  acl    = "private"
}

resource aws_ssm_parameter jobs_bucket {
  name  = "/buckets/jobs_bucket"
  type  = "SecureString"
  value = var.jobs_bucket
}
