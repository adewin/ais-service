resource aws_iam_policy store_put_object {
  name_prefix = var.function_name
  policy      = <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "s3:PutObject*",
        "s3:List*",
      ],
      "Resource": [
        "arn:aws:s3:::${var.store_name}",
        "arn:aws:s3:::${var.store_name}/*"
      ]
    }
  ]
}
EOF
}
resource aws_iam_role_policy_attachment store_get_object_attachment {
  role = var.function_execution_role_name
  policy_arn = aws_iam_policy.store_put_object.arn
}

