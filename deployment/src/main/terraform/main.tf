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
  source                         = "./modules/storage"
  data_query_results_store_name  = data.external.secrets.result["data_query_results_store"]
  raw_ais_store_name             = data.external.secrets.result["raw_ais_store"]
  ais_data_upload_store_name     = data.external.secrets.result["ais_data_upload_store"]
  raw_partitioned_ais_store_name = data.external.secrets.result["raw_partitioned_ais_store"]
}

module functions {
  source                       = "./modules/functions"
  ais_data_upload_store_name   = data.external.secrets.result["ais_data_upload_store"]
  ais_raw_store_name           = data.external.secrets.result["raw_ais_store"]
  data_upload_function_jar     = var.DATA_FILE_FUNCTION_LAMBDA_JAR_PATH
  emr_all_jobs_function_jar    = var.AIS_BATCH_FUNCTION_JAR_PATH
  emr_logs_store_name          = data.external.secrets.result["emr_logs_bucket"]
  heatmap_store_name           = data.external.secrets.result["sensitive_heatmap_bucket"]
  jobs_store_name              = data.external.secrets.result["jobs_bucket"]
  sensitive_heatmap_store_name = data.external.secrets.result["sensitive_heatmap_bucket"]
  spark_job_jar_name           = var.SPARK_JOB_JAR_NAME
}

module notifications {
  source = "./modules/notifications"
}

module data_query {
  source                        = "./modules/data-query"
  data_query_name               = data.external.secrets.result["data_query_name"]
  data_query_results_store_name = data.external.secrets.result["data_query_results_store"]
  catalog_database_name         = "ukho-ais-data"
  catalog_database_table_name   = "raw_ais_data"
  data_prefix                   = ""
  data_store_name               = data.external.secrets.result["raw_partitioned_ais_store"]
}
