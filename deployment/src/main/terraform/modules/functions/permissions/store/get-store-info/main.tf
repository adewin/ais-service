resource aws_iam_policy get_store_info {
  name_prefix = "${var.function_name}GetStoreInfo"
  policy      = <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "s3:GetBucketAcl"
      ],
      "Resource": "*"
    }
  ]
}
EOF
}

resource aws_iam_role_policy_attachment get_store_info_attachment {
  role = var.function_execution_role_name
  policy_arn = aws_iam_policy.get_store_info.arn
}

