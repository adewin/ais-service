resource aws_s3_bucket jobs_bucket {
  bucket = var.jobs_bucket
  acl    = "private"
}

resource aws_ssm_parameter jobs_bucket {
  name  = "/buckets/jobs_bucket"
  type  = "SecureString"
  value = var.jobs_bucket
}

resource aws_s3_bucket_object spark_s3_job {
  bucket = aws_s3_bucket.jobs_bucket.bucket
  key    = var.spark_job_jar_name
  source = var.spark_job_jar_path
}
