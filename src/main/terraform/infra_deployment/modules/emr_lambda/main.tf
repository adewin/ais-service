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
  filename      = "${path.module}/emr_dummy.zip"
  function_name = "emr_orchestration"
  role          = "${aws_iam_role.iam_for_emr_lambda.arn}"
  handler       = "uk.gov.ukho.aisbatchlambda.AisBatchLambdaHandler"
  runtime       = "java8"
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
