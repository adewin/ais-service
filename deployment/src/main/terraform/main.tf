provider aws {
  region = "eu-west-2"
}

terraform {
  backend s3 {
    bucket         = "ais-to-raster-terra-state"
    key            = "state"
    region         = "eu-west-2"
    dynamodb_table = "ais-to-heatmap-terraform-lock-table"
  }
}

# DEPRECATED - Use storage for future buckets
module s3_buckets {
  source                   = "./modules/legacy-buckets"
  jobs_bucket              = data.external.secrets.result["jobs_bucket"]
  heatmap_bucket           = data.external.secrets.result["heatmap_bucket"]
  sensitive_heatmap_bucket = data.external.secrets.result["sensitive_heatmap_bucket"]
  emr_logs_bucket          = data.external.secrets.result["emr_logs_bucket"]
  spark_job_jar_path       = var.SPARK_JOB_JAR_PATH
  spark_job_jar_name       = var.SPARK_JOB_JAR_NAME
}

module storage {
  source                     = "./modules/storage"
  raw_ais_store_name         = data.external.secrets.result["raw_ais_bucket"]
  ais_data_upload_store_name = data.external.secrets.result["ais_data_upload_bucket"]
}

module emr_lambda {
  source                   = "./modules/emr_lambda"
  jar                      = var.AIS_BATCH_LAMBDA_JAR_PATH
  jobs_bucket              = data.external.secrets.result["jobs_bucket"]
  ais_bucket               = data.external.secrets.result["raw_ais_bucket"]
  heatmap_bucket           = data.external.secrets.result["heatmap_bucket"]
  sensitive_heatmap_bucket = data.external.secrets.result["sensitive_heatmap_bucket"]
  emr_logs_bucket          = data.external.secrets.result["emr_logs_bucket"]
  spark_job_jar_name       = var.SPARK_JOB_JAR_NAME
}

module notifications {
  source = "./modules/notifications"
}
