module data_query_perms {
  source           = "./permissions/data-query"
  resource_prefix  = "DataQuery"
  results_store_id = var.data_query_results_store_id
}

module catalog {
  source                      = "./catalog"
  catalog_database_name       = var.catalog_database_name
  catalog_database_table_name = var.catalog_database_table_name
  data_prefix                 = var.data_prefix
  data_store_name             = var.data_store_name
}
