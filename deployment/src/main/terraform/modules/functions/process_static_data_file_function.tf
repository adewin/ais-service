module process_static_data_file_function {
  source           = "./function"
  function_name    = "process-static-data-file-handler"
  function_handler = "process_new_static_file.lambda_handler.lambda_handler"
  function_code    = var.process_static_data_file_zip
  runtime          = "python3.7"
  timeout          = 900
  memory           = 2048

  function_environment_variables = {
    DESTINATION_BUCKET     = var.processed_static_data_store_name
    DESTINATION_KEY_PREFIX = "data"
    OUTPUT_FILE_EXTENSION  = ".csv.bz2"
    BUFFER_SIZE_MB         = 512
  }
}

module process_static_data_file_read_permissions {
  source                       = "./permissions/store/get-objects"
  function_execution_role_name = module.process_static_data_file_function.function_execution_role_name
  function_name                = module.process_static_data_file_function.function_name
  store_name                   = var.static_data_store_name
}

module process_static_data_file_write_permissions {
  source                       = "./permissions/store/put-objects"
  function_execution_role_name = module.process_static_data_file_function.function_execution_role_name
  function_name                = module.process_static_data_file_function.function_name
  store_name                   = var.processed_static_data_store_name
}

module process_static_data_file_upload_trigger {
  source      = "./triggers/storage_file_upload"
  function_id = module.process_static_data_file_function.function_id
  store_name  = var.static_data_store_name
  item_prefix = "data/"
}
