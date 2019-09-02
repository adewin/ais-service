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

module network {
  source = "./modules/network"
}

# DEPRECATED - Use storage for future buckets
module s3_buckets {
  source                   = "./modules/legacy-buckets"
  jobs_bucket              = data.external.secrets.result["jobs_bucket"]
  heatmap_bucket           = data.external.secrets.result["heatmap_bucket"]
  sensitive_heatmap_bucket = data.external.secrets.result["sensitive_heatmap_bucket"]
  emr_logs_bucket          = data.external.secrets.result["emr_logs_bucket"]
}

module batch {
  source                      = "./modules/batch"
  heatmap_store               = data.external.secrets.result["heatmap_bucket"]
  network_subnet_ids          = module.network.network_subnet_ids
  network_security_group_ids  = [module.network.network_security_group_id]
  docker_registry_url         = var.DOCKER_REGISTRY_URL
  project_version             = var.PROJECT_VERSION
  data_query_access_policy_id = module.data_query.data_query_access_policy_id
}

module storage {
  source                               = "./modules/storage"
  ais_data_upload_store_name           = data.external.secrets.result["ais_data_upload_store"]
  data_query_results_store_name        = data.external.secrets.result["data_query_results_store"]
  raw_ais_store_name                   = data.external.secrets.result["raw_ais_store"]
  raw_partitioned_ais_store_name       = data.external.secrets.result["raw_partitioned_ais_store"]
  resampled_partitioned_ais_store_name = data.external.secrets.result["resampled_partitioned_ais_store"]
  static_data_store_name               = data.external.secrets.result["static_data_store"]
  static_data_upload_store_name        = data.external.secrets.result["static_data_upload_store"]
  processed_static_data_store_name     = data.external.secrets.result["processed_static_data_store"]
}

module partitioning_spark_jar {
  source     = "./modules/storage/file"
  file_name  = var.PARTITIONING_SPARK_JOB_JAR_NAME
  file_path  = var.PARTITIONING_SPARK_JOB_JAR_PATH
  store_name = data.external.secrets.result["jobs_bucket"]
}

module functions {
  source                               = "./modules/functions"
  ais_data_upload_store_name           = data.external.secrets.result["ais_data_upload_store"]
  ais_raw_store_name                   = data.external.secrets.result["raw_ais_store"]
  emr_logs_store_name                  = data.external.secrets.result["emr_logs_bucket"]
  heatmap_store_name                   = data.external.secrets.result["sensitive_heatmap_bucket"]
  jobs_store_name                      = data.external.secrets.result["jobs_bucket"]
  sensitive_heatmap_store_name         = data.external.secrets.result["sensitive_heatmap_bucket"]
  ais_raw_partitioned_store_name       = data.external.secrets.result["raw_partitioned_ais_store"]
  ais_resampled_partitioned_store_name = data.external.secrets.result["resampled_partitioned_ais_store"]
  trigger_raw_partition_jar            = var.TRIGGER_RAW_PARTITION_FUNCTION_LAMBDA_JAR_PATH
  data_upload_function_jar             = var.DATA_FILE_FUNCTION_LAMBDA_JAR_PATH
  partitioning_spark_job_jar_name      = var.PARTITIONING_SPARK_JOB_JAR_NAME
  static_data_store_name               = data.external.secrets.result["static_data_store"]
  static_data_upload_store_name        = data.external.secrets.result["static_data_upload_store"]
  process_static_data_file_zip         = var.PROCESS_STATIC_DATA_ZIP_PATH
  processed_static_data_store_name     = data.external.secrets.result["processed_static_data_store"]
}

module notifications {
  source = "./modules/notifications"
}

module data_query {
  source                                       = "./modules/data-query"
  ais_database_name                            = "ukho_ais_data"
  processed_ais_table_name                     = "processed_ais_data"
  processed_ais_store_name                     = data.external.secrets.result["raw_partitioned_ais_store"]
  processed_static_data_table_name             = "processed_static_data"
  processed_static_data_store_name             = data.external.secrets.result["processed_static_data_store"]
  osd_ais_table_name                           = "osd_ais_data"
  osd_ais_store_name                           = data.external.secrets.result["raw_ais_store"]
  data_query_results_store_id                  = module.storage.data_query_results_store_id
  derived_resampled_100km_18hr_ais_data_prefix = "100km_18hr/"
  derived_resampled_100km_18hr_ais_table_name  = "derived_resampled_100km_18hr_ais_data"
  derived_resampled_30km_6hr_ais_data_prefix   = "30km_6hr/"
  derived_resampled_30km_6hr_ais_table_name    = "derived_resampled_30km_6hr_ais_data"
  derived_resampled_ais_store_name             = data.external.secrets.result["resampled_partitioned_ais_store"]
}
