resource "aws_iam_role" "iam_for_emr_lambda" {
  name = "iam_for_emr_lambda"

  assume_role_policy = <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Action": "sts:AssumeRole",
      "Principal": {
        "Service": "lambda.amazonaws.com"
      },
      "Effect": "Allow",
      "Sid": ""
    }
  ]
}
EOF
}

resource "aws_lambda_function" "emr_lambda" {
  filename = "${path.module}/emr_dummy.zip"
  function_name = "emr_orchestration"
  role = "${aws_iam_role.iam_for_emr_lambda.arn}"
  handler = "uk.gov.ukho.aisbatchlambda.AisBatchLambdaHandler"
  runtime = "java8"
  timeout = 303
  memory_size = 256

  environment {
    variables = {
      JOB_FULLY_QUALIFIED_CLASS_NAME = "uk.gov.ukho.ais.rasters.AisToRaster"
      JOB_LOCATION = "s3://${var.jobs_bucket_name}/ais-rasters-1.1-SNAPSHOT-all.jar"
      INPUT_LOCATION = "s3://${var.ais_bucket_name}/*"
      OUTPUT_LOCATION = "${var.heatmap_bucket_name}"
      RESOLUTION = "1"
      INSTANCE_TYPE = "m4.large"
      LOG_URI = "s3://${var.emr_logs_bucket_name}/"
      SERVICE_ROLE = "EMR_DefaultRole"
      JOB_FLOW_ROLE = "EMR_EC2_DefaultRole"
      CLUSTER_NAME = "AIS Heatmap Cluster"
      EMR_VERSION = "emr-5.20.0"
      INSTANCE_COUNT = "3"
    }
  }
}

resource "aws_cloudwatch_log_group" "emr_lambda_log_group" {
  name = "/aws/lambda/${aws_lambda_function.emr_lambda.function_name}"
}

resource "aws_iam_policy" "emr_lambda_logging" {
  name = "emr_lambda_logging"
  path = "/"

  policy = <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Action": [
        "logs:CreateLogStream",
        "logs:PutLogEvents"
      ],
      "Resource": "arn:aws:logs:*:*:*",
      "Effect": "Allow"
    }
  ]
}
EOF
}

resource "aws_iam_role_policy_attachment" "lambda_logs" {
  role       = "${aws_iam_role.iam_for_emr_lambda.name}"
  policy_arn = "${aws_iam_policy.emr_lambda_logging.arn}"
}

resource "aws_iam_role_policy_attachment" "emr_access" {
  role       = "${aws_iam_role.iam_for_emr_lambda.name}"
  policy_arn = "arn:aws:iam::aws:policy/AmazonElasticMapReduceFullAccess"
}