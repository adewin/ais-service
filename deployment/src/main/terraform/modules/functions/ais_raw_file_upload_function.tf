module ais_raw_file_upload_function {
  source           = "./function"
  function_name    = "ais-data-upload-handler"
  function_handler = "uk.gov.ukho.ais.ingestuploadfile.FileUploadLambdaRequestHandler"
  function_code    = var.data_upload_function_jar
  function_environment_variables = {
    DESTINATION_BUCKET     = var.ais_raw_store_name
    DESTINATION_KEY_PREFIX = "data/"
  }
}

module ais_raw_file_upload_function_get_uploaded_file_permission {
  source                       = "./permissions/store/get-objects"
  function_name                = module.ais_raw_file_upload_function.function_name
  function_execution_role_name = module.ais_raw_file_upload_function.function_execution_role_name
  store_name                   = var.ais_data_upload_store_name
}

module ais_raw_file_upload_function_put_uploaded_file_permission {
  source                       = "./permissions/store/put-objects"
  function_name                = module.ais_raw_file_upload_function.function_name
  function_execution_role_name = module.ais_raw_file_upload_function.function_execution_role_name
  store_name                   = var.ais_raw_store_name
}

module ais_raw_file_upload_store_trigger {
  source      = "./triggers/storage_file_upload"
  function_id = module.ais_raw_file_upload_function.function_id
  store_name  = var.ais_data_upload_store_name
}
