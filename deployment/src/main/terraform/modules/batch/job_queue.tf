resource "aws_batch_job_queue" "queue" {
  name                 = "queue"
  state                = "ENABLED"
  priority             = 1
  compute_environments = [aws_batch_compute_environment.ukho-batch-compute-enviroment.arn]
  depends_on           = [aws_batch_compute_environment.ukho-batch-compute-enviroment]
}
