
module trigger_resample_function {
  source           = "./function"
  function_handler = "uk.gov.ukho.ais.triggerresamplelambda.TriggerResampleLambdaHandler"
  function_name    = "trigger_resampling"
  jar              = var.trigger_resample_jar
  function_environment_variables = {
    INSTANCE_TYPE_MASTER           = "m5.xlarge"
    INSTANCE_TYPE_WORKER           = "m5.xlarge"
    LOG_URI                        = "s3://${var.emr_logs_store_name}/"
    SERVICE_ROLE                   = "EMR_DefaultRole"
    JOB_FLOW_ROLE                  = "EMR_EC2_DefaultRole"
    CLUSTER_NAME                   = "AIS Raw Resampling Cluster"
    EMR_VERSION                    = "emr-5.20.0"
    INSTANCE_COUNT                 = "9"
    DRIVER_MEMORY                  = "11000m"
    EXECUTOR_MEMORY                = "11000m"
    QUEUE_URL                      = var.new_partitioned_raw_queue_url
    JOB_FULLY_QUALIFIED_CLASS_NAME = "uk.gov.ukho.ais.resample.ResampleAis"
    JOB_LOCATION                   = "s3://${var.jobs_store_name}/${var.resampling_spark_job_jar_name}"
    INPUT_LOCATION                 = "s3a://${var.ais_raw_partitioned_store_name}"
    OUTPUT_LOCATION                = "s3a://${var.ais_resampled_partitioned_store_name}/"
  }
}

module trigger_resample_spark_job_permissions {
  source                       = "./permissions/run-spark-job"
  function_execution_role_name = module.trigger_resample_function.function_execution_role_name
}

module trigger_resample_store_permissions {
  source                       = "./permissions/store/remove-objects"
  function_execution_role_name = module.trigger_resample_function.function_execution_role_name
  function_name                = module.trigger_resample_function.function_name
  store_name                   = var.ais_resampled_partitioned_store_name
}

module trigger_resample_function_trigger {
  source          = "./triggers/time_based"
  cron_expression = "0 21 ? * MON-FRI *"
  function_id     = module.trigger_resample_function.function_id
  function_name   = module.trigger_resample_function.function_name
}
