module derived_resampled_30km_6hr_ais_table {
  source        = "./table"
  database_name = var.database_name
  store_name    = var.derived_resampled_ais_store_name
  table_name    = var.derived_resampled_30km_6hr_ais_table_name
  data_prefix   = var.derived_resampled_30km_6hr_ais_data_prefix
}
