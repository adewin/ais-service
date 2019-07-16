module ais_raw_partitioned_catalog_table {
  source        = "./table"
  database_name = var.ais_catalog_database_name
  store_name    = var.ais_raw_partitioned_store_name
  table_name    = var.ais_raw_catalog_database_table_name
}
