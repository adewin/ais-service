output function_id {
  value = aws_lambda_function.lambda_function.arn
}

output function_name {
  value = aws_lambda_function.lambda_function.function_name
}

output function_execution_role_name {
  value = aws_iam_role.lambda_role.name
}
