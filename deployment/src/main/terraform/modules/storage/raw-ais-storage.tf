module ais_upload_store {
  source       = "./store"
  store_name   = var.ais_data_upload_store_name
  logical_name = "raw-ais"
}

module raw_ais_full_perms {
  source          = "./permissions/full"
  store_id        = module.ais_upload_store.store_id
  resource_prefix = "RawAis"
}
