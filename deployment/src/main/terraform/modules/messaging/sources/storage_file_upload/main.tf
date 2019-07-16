resource aws_s3_bucket_notification storage_file_upload_trigger {
  bucket = var.store_name

  queue {
    queue_arn     = var.queue_id
    events        = ["s3:ObjectCreated:*"]
    filter_suffix = var.item_suffix
  }
}
