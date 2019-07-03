resource aws_s3_bucket bucket {
  bucket = var.store_name
  acl    = "private"
}

resource aws_ssm_parameter bucket_parameter {
  name  = "/buckets/${var.logical_name}"
  type  = "SecureString"
  value = aws_s3_bucket.bucket.bucket
}
