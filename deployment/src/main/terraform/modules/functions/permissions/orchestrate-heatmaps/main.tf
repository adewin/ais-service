resource aws_iam_role_policy_attachment emr_access {
  role       = var.function_execution_role_name
  policy_arn = "arn:aws:iam::aws:policy/AmazonElasticMapReduceFullAccess"
}
