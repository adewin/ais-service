provider "aws" {
  region = "eu-west-2"
}

terraform {
  backend "s3" {
    bucket         = "ais-to-raster-terra-state"
    key            = "state"
    region         = "eu-west-2"
    dynamodb_table = "ais-to-heatmap-terraform-lock-table"
  }
}

module "s3_buckets" {
  source         = "./modules/buckets"
  jobs_bucket    = "${data.external.secrets.result["jobs_bucket"]}"
  ais_bucket     = "${data.external.secrets.result["ais_bucket"]}"
  heatmap_bucket = "${data.external.secrets.result["heatmap_bucket"]}"
}

module "emr_lambda" {
  source = "./modules/emr_lambda"
}

module "notifications" {
  source = "./modules/notifications"
}
