resource aws_iam_policy queue_receive_and_purge_messages {
  name_prefix = var.function_name
  policy      = <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "sqs:ReceiveMessage",
        "sqs:PurgeQueue"
      ],
      "Resource": [
        "arn:aws:sqs:*:*:*"
      ]
    }
  ]
}
EOF
}

resource aws_iam_role_policy_attachment queue_receive_and_purge_messages_attachment {
  role = var.function_execution_role_name
  policy_arn = aws_iam_policy.queue_receive_and_purge_messages.arn
}
