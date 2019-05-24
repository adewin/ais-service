resource "aws_s3_bucket" "jobs_bucket" {
  bucket = "${var.jobs_bucket}"
  acl    = "private"
}

resource "aws_s3_bucket" "ais_bucket" {
  bucket = "${var.ais_bucket}"
  acl    = "private"
}

resource "aws_s3_bucket" "heatmap_bucket" {
  bucket = "${var.heatmap_bucket}"
  acl    = "private"
}

resource "aws_s3_bucket" "emr_log_bucket" {
  bucket = "${var.emr_log_bucket}"
  acl    = "private"
}

resource "aws_ssm_parameter" "jobs_bucket" {
  name  = "/buckets/jobs_bucket"
  type  = "SecureString"
  value = "${var.jobs_bucket}"
}

resource "aws_ssm_parameter" "ais_bucket" {
  name  = "/buckets/ais_bucket"
  type  = "SecureString"
  value = "${var.ais_bucket}"
}

resource "aws_ssm_parameter" "heatmap_bucket" {
  name  = "/buckets/heatmap_bucket"
  type  = "SecureString"
  value = "${var.heatmap_bucket}"
}

resource "aws_ssm_parameter" "emr_log_bucket" {
  name  = "/buckets/emr_log_bucket"
  type  = "SecureString"
  value = "${var.emr_log_bucket}"
}
