output queue_id {
  value = aws_sqs_queue.new_partitioned_file_queue.arn
}
