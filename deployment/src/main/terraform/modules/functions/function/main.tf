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

resource aws_iam_role_policy_attachment lambda_logs {
  policy_arn = aws_iam_role.lambda_role.name
  role = "arn:aws:iam::aws:policy/service-role/AWSLambdaBasicExecutionRole"
}

