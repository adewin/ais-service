resource aws_athena_database athena_database {
  bucket = var.data_query_results_store_name
  name   = var.data_query_name
}

module data_query_perms {
  source          = "./permissions/data-query"
  database_name   = aws_athena_database.athena_database.id
  resource_prefix = "DataQuery"
}

module catalog {
  source                      = "./catalog"
  catalog_database_name       = var.catalog_database_name
  catalog_database_table_name = var.catalog_database_table_name
  data_prefix                 = var.data_prefix
  data_store_name             = var.data_store_name
}
