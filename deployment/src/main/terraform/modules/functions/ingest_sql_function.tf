module ais_sql_ingest_function {
  source           = "./function"
  function_name    = "ingest-sql-file-handler"
  function_handler = "uk.gov.ukho.ais.ingestsqlfile.IngestSqlFileLambdaHandler"
  function_code    = var.ais_sql_ingest_function_jar
}

module ais_sql_ingest_function_get_uploaded_file_permission {
  source                       = "./permissions/store/get-objects"
  function_name                = module.ais_sql_ingest_function.function_name
  function_execution_role_name = module.ais_sql_ingest_function.function_execution_role_name
  store_name                   = var.heatmap_sql_store_name
}

module ais_sql_ingest_function_put_uploaded_file_permission {
  source                       = "./permissions/store/put-objects"
  function_name                = module.ais_sql_ingest_function.function_name
  function_execution_role_name = module.ais_sql_ingest_function.function_execution_role_name
  store_name                   = var.heatmap_sql_store_name
}

module ais_sql_ingest_function_store_trigger {
  source      = "./triggers/storage_file_upload"
  function_id = module.ais_sql_ingest_function.function_id
  store_name  = var.heatmap_sql_store_name
}
