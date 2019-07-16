module new_partitioned_file_queue {
  source            = "./queue"
  message_source_id = var.ais_raw_partitioned_store_id
  queue_name_prefix = "new-partitioned-raw"
}

module new_partitioned_file_message_source {
  source      = "./sources/storage_file_upload"
  item_suffix = "SUCCESS"
  queue_id    = module.new_partitioned_file_queue.queue_id
  store_name  = var.ais_raw_partitioned_store_name
}
