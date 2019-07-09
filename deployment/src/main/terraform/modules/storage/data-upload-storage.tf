module ais_data_upload_store {
  source     = "./store"
  store_name = var.raw_ais_store_name
}

module ais_data_upload_full_perms {
  source          = "./permissions/full"
  store_id        = module.ais_data_upload_store.store_id
  resource_prefix = "DataUpload"
}
