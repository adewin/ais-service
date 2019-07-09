module trigger_raw_partition_function {
  source           = "./function"
  function_name    = "trigger-raw-partition"
  function_handler = "uk.gov.ukho.ais.triggerrawpartitioning.TriggerRawPartitioningLambdaHandler"
  jar              = var.trigger_raw_partition_jar
  function_environment_variables = {
    JOB_LOCATION                   = "s3://${var.jobs_store_name}/${var.partitioning_spark_job_jar_name}"
    INSTANCE_TYPE_MASTER           = "m4.4xlarge"
    INSTANCE_TYPE_WORKER           = "m4.2xlarge"
    LOG_URI                        = "s3://${var.emr_logs_store_name}/"
    SERVICE_ROLE                   = "EMR_DefaultRole"
    JOB_FLOW_ROLE                  = "EMR_EC2_DefaultRole"
    CLUSTER_NAME                   = "AIS Raw Partitioning Cluster"
    EMR_VERSION                    = "emr-5.20.0"
    INSTANCE_COUNT                 = "5"
    DRIVER_MEMORY                  = "50g"
    EXECUTOR_MEMORY                = "16g"
    JOB_FULLY_QUALIFIED_CLASS_NAME = "uk.gov.ukho.ais.partitioning.PartitionRawAis"
    OUTPUT_LOCATION                = var.ais_raw_partitioned_store_name
  }
}

module trigger_raw_partition_read_raw_data_permissions {
  source                       = "./permissions/store-get-objects"
  function_execution_role_name = module.trigger_raw_partition_function.function_execution_role_name
  function_name                = module.trigger_raw_partition_function.function_name
  store_name                   = var.ais_raw_store_name
}

module trigger_partition_spark_job_permissions {
  source                       = "./permissions/run-spark-job"
  function_execution_role_name = module.trigger_raw_partition_function.function_execution_role_name
}

module trigger_raw_partition_s3_trigger {
  source      = "./triggers/storage_file_upload"
  function_id = module.trigger_raw_partition_function.function_id
  store_name  = var.ais_raw_store_name
}
