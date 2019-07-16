module resampled_partitioned_ais_table {
  source        = "./table"
  database_name = var.ais_catalog_database_name
  store_name    = var.resampled_partitioned_ais_store_name
  table_name    = var.resampled_partitioned_ais_catalog_database_table_name
}
