resource aws_s3_bucket emr_logs_bucket {
  bucket = var.emr_logs_bucket
  acl    = "private"
}

resource aws_ssm_parameter emr_logs_bucket {
  name  = "/buckets/emr_logs_bucket"
  type  = "SecureString"
  value = var.emr_logs_bucket
}
