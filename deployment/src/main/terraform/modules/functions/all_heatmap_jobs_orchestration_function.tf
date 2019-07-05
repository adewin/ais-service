
module emr_all_jobs_function {
  source           = "./function"
  function_name    = "emr_all_jobs_orchestration"
  jar              = var.emr_all_jobs_function_jar
  function_handler = "uk.gov.ukho.aisbatchlambda.AisBatchLambdaHandler"
  function_environment_variables = {
    JOB_FULLY_QUALIFIED_CLASS_NAME = "uk.gov.ukho.ais.rasters.AisToRaster"
    JOB_LOCATION                   = "s3://${var.jobs_store_name}/${var.spark_job_jar_name}"
    INPUT_LOCATION                 = "s3://${var.ais_raw_store_name}/data/"
    OUTPUT_LOCATION                = var.heatmap_store_name
    INSTANCE_TYPE_MASTER           = "m4.4xlarge"
    INSTANCE_TYPE_WORKER           = "m4.2xlarge"
    LOG_URI                        = "s3://${var.emr_logs_store_name}/"
    SERVICE_ROLE                   = "EMR_DefaultRole"
    JOB_FLOW_ROLE                  = "EMR_EC2_DefaultRole"
    CLUSTER_NAME                   = "AIS Heatmap Cluster"
    EMR_VERSION                    = "emr-5.20.0"
    INSTANCE_COUNT                 = "21"
    DRIVER_MEMORY                  = "50g"
    EXECUTOR_MEMORY                = "16g"
    SENSITIVE_OUTPUT_LOCATION      = var.sensitive_heatmap_store_name
    DRAUGHT_CONFIG_FILE            = "s3://${var.jobs_store_name}/draughts.config"
    STATIC_DATA_FILE               = "s3://${var.ais_raw_store_name}/static/arkevista_static.txt.bz2"
  }
}

module emr_orchestration_permissions {
  source                     = "./permissions/orchestrate-heatmaps"
  function_execution_role_id = module.emr_all_jobs_function.function_execution_role_id
}
