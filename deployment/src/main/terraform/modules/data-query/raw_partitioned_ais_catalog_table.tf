module ais_raw_partitioned_catalog_table {
  source        = "./table"
  database_name = var.ais_catalog_database_name
  store_name    = var.ais_raw_partitioned_store_name
  table_name    = var.ais_raw_catalog_database_table_name
}

resource aws_athena_named_query original_source_file_start_end_query {
  database = var.ais_catalog_database_name
  name     = "original_source_file_start_end_query"
  query    = <<EOF
SELECT input_ais_data_file, min(acquisition_time) AS START_DATE, max(acquisition_time) AS END_DATE
FROM "${var.ais_catalog_database_name}"."${var.ais_raw_catalog_database_table_name}"
GROUP BY input_ais_data_file;
EOF
}
