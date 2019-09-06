module static_data_upload_store {
  source     = "./store"
  store_name = var.static_data_upload_store_name
}

module static_data_upload_storage_full_perms {
  source          = "./permissions/full"
  store_id        = module.static_data_upload_store.store_id
  resource_prefix = "StaticUpload"
}
