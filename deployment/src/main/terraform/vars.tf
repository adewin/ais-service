
data "external" "secrets" {
  program = [
    "openssl",
    "enc",
    "-aes-256-cbc",
    "-d",
    "-md",
    "md5",
    "-k",
    "${var.PASSWORD}",
    "-in",
    "${path.module}/parameters.json.enc",
  ]
}

variable "DATA_FILE_FUNCTION_LAMBDA_JAR_PATH" {}
variable "TRIGGER_RAW_PARTITION_FUNCTION_LAMBDA_JAR_PATH" {}
variable "PARTITIONING_SPARK_JOB_JAR_NAME" {}
variable "PARTITIONING_SPARK_JOB_JAR_PATH" {}
variable "PASSWORD" {}
variable "PROCESS_STATIC_DATA_ZIP_PATH" {}
