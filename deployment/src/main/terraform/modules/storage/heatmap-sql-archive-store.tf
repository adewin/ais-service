module heatmap_sql_archive_store {
  source     = "./store"
  store_name = var.static_data_store_name
}

module heatmap_sql_archive_store_read_only_perms {
  source          = "./permissions/read-only"
  store_id        = module.heatmap_sql_archive_store.store_id
  resource_prefix = "HeatmapSQLArchive"
}
