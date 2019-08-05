resource aws_iam_role lambda_role {
  name               = "${var.function_name}ExecutionRole"
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

resource aws_lambda_function lambda_function {
  function_name = var.function_name
  filename = var.function_code
  handler = var.function_handler
  role = aws_iam_role.lambda_role.arn
  runtime = var.runtime
  memory_size = var.memory
  timeout = var.timeout
  environment {
    variables = var.function_environment_variables
  }
}

resource aws_cloudwatch_log_group lambda_log_group {
  name = "/aws/lambda/${aws_lambda_function.lambda_function.function_name}"
}

resource aws_iam_policy lambda_logging {
  name = "${var.function_name}_logging"
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

resource aws_iam_role_policy_attachment lambda_logs {
  role       = aws_iam_role.lambda_role.name
  policy_arn = aws_iam_policy.lambda_logging.arn
}
