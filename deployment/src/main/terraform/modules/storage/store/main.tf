resource aws_s3_bucket bucket {
  bucket = var.store_name
  acl    = "private"
}
