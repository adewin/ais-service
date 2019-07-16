resource aws_sqs_queue new_partitioned_file_queue {
  name_prefix = var.queue_name_prefix
  policy      = <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Principal": "*",
      "Action": "sqs:SendMessage",
      "Resource": "arn:aws:sqs:*:*:s3-event-notification-queue",
      "Condition": {
        "ArnEquals": { "aws:SourceArn": "${var.message_source_id}" }
      }
    }
  ]
}
EOF
}
