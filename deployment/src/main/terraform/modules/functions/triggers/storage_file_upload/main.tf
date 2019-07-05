resource aws_lambda_permission storage_file_upload_trigger {
  statement_id  = "AllowFileUploadTo${var.store_name}"
  action        = "lambda:InvokeFunction"
  function_name = var.function_id
  principal     = "s3.amazonaws.com"
  source_arn    = "arn:aws:s3:::${var.store_name}"
}
