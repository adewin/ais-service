resource aws_s3_bucket_object s3_object {
  bucket = var.store_name
  key    = var.file_name
  source = var.file_path
}
