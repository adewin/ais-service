module validate_job_config_function {
  source           = "./function"
  function_code    = var.validate_job_config_jar
  function_handler = "uk.gov.ukho.ais.validatenewjobconfiglambda.ValidateJobConfigLambdaHandler"
  function_name    = "ValidateJobConfig"
  function_environment_variables = {
    FILTER_SQL_ARCHIVE_BUCKET_NAME = var.heatmap_sql_store_name
    FILTER_SQL_ARCHIVE_PREFIX      = var.heatmap_sql_archive_prefix
  }
}

module validate_job_config_read_job_permissions {
  source                       = "./permissions/store/get-objects"
  function_execution_role_name = module.validate_job_config_function.function_id
  function_name                = module.validate_job_config_function.function_name
  store_name                   = var.heatmap_job_submission_store_name
}

module validate_job_config_validate_sql_file_permissions {
  source                       = "./permissions/store/get-objects"
  function_execution_role_name = module.validate_job_config_function.function_id
  function_name                = module.validate_job_config_function.function_name
  store_name                   = var.heatmap_sql_store_name
}

module validate_job_config_get_store_info {
  source                       = "./permissions/store/get-store-info"
  function_execution_role_name = module.validate_job_config_function.function_id
  function_name                = module.validate_job_config_function.function_name
}
