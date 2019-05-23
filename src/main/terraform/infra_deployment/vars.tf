variable "PASSWORD" {}

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
