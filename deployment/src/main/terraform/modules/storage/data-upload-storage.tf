module ais_data_upload_store {
  source       = "./store"
  store_name   = var.raw_ais_store_name
  logical_name = "raw-ais"
}

module ais_data_upload_read_only_perms {
  source          = "./permissions/read-only"
  store_id        = module.ais_data_upload_store.store_id
  resource_prefix = "RawAis"
}
