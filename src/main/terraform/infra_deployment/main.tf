provider "aws" {
  region = "eu-west-2"
}

terraform {
  backend "s3" {
    bucket = "ais-to-raster-terra-state"
    key    = "state"
    region = "eu-west-2"
  }
}

resource "aws_s3_bucket" "jobs_bucket" {
  bucket = "${data.external.secrets.result["jobs_bucket"]}"
  acl = "private"
}

resource "aws_s3_bucket" "ais_bucket" {
  bucket = "${data.external.secrets.result["ais_bucket"]}"
  acl = "private"
}

resource "aws_s3_bucket" "heatmap_bucket" {
  bucket = "${data.external.secrets.result["heatmap_bucket"]}"
  acl = "private"
}

resource "aws_ssm_parameter" "jobs_bucket" {
  name = "/buckets/jobs_bucket"
  type = "SecureString"
  value = "${aws_s3_bucket.jobs_bucket.bucket}"
}

resource "aws_ssm_parameter" "ais_bucket" {
  name = "/buckets/ais_bucket"
  type = "SecureString"
  value = "${aws_s3_bucket.ais_bucket.bucket}"
}

resource "aws_ssm_parameter" "heatmap_bucket" {
  name = "/buckets/heatmap_bucket"
  type = "SecureString"
  value = "${aws_s3_bucket.heatmap_bucket.bucket}"
}
