module osd_ais_table {
  source        = "./table"
  database_name = var.database_name
  store_name    = var.osd_ais_store_name
  table_name    = var.osd_ais_table_name
}
