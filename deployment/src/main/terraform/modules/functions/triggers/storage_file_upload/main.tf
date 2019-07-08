resource aws_lambda_permission storage_file_upload_trigger_permission {
  statement_id  = "AllowFileUploadTo${var.store_name}"
  action        = "lambda:InvokeFunction"
  function_name = var.function_id
  principal     = "s3.amazonaws.com"
  source_arn    = "arn:aws:s3:::${var.store_name}"
}

resource aws_s3_bucket_notification storage_file_upload_trigger {
  bucket = var.store_name

  lambda_function {
    lambda_function_arn = var.function_id
    events              = ["s3:ObjectCreated:*"]
  }
}
