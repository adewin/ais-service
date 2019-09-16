
resource aws_iam_policy start_state_machine_execution {
  name_prefix = var.function_name
  policy      = <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": "states:StartExecution",
      "Resource": "${var.state_machine_id}"
    }
  ]
}
EOF
}
resource aws_iam_role_policy_attachment store_get_object_attachment {
  role = var.function_execution_role_name
  policy_arn = aws_iam_policy.start_state_machine_execution.arn
}
