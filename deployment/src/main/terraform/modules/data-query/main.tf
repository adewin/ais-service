module data_query_perms {
  source           = "./permissions/data-query"
  resource_prefix  = "DataQuery"
  results_store_id = var.data_query_results_store_id
}

resource aws_glue_catalog_database catalog_database {
  name = var.database_name
}
