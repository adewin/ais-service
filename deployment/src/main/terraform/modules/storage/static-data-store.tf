module static_data_store {
  source     = "./store"
  store_name = var.static_data_store_name
}

module static_data_storage_read_only_perms {
  source          = "./permissions/read-only"
  store_id        = module.static_data_store.store_id
  resource_prefix = "Static"
}
