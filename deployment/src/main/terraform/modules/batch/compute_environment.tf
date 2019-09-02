resource aws_iam_role ecs_instance_role {
  name = "ecs_instance_role"

  assume_role_policy = <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Action": "sts:AssumeRole",
      "Effect": "Allow",
      "Principal": {
        "Service": "ec2.amazonaws.com"
      }
    }
  ]
}
EOF
}

resource aws_iam_role_policy_attachment default_policy_attach {
  role = aws_iam_role.ecs_instance_role.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonEC2ContainerServiceforEC2Role"
}

resource aws_iam_instance_profile ecs_instance_profile {
  name = "ecs_instance_role"
  role = aws_iam_role.ecs_instance_role.name
}

resource aws_iam_role aws_batch_service_role {
  name = "aws_batch_service_role"

  assume_role_policy = <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Action": "sts:AssumeRole",
      "Effect": "Allow",
      "Principal": {
        "Service": "batch.amazonaws.com"
      }
    }
  ]
}
EOF
}

resource aws_iam_role_policy_attachment aws_batch_service_role {
  role       = aws_iam_role.aws_batch_service_role.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AWSBatchServiceRole"
}

resource aws_iam_role_policy_attachment data_query_policy_attachment {
  role       = aws_iam_role.ecs_instance_role.name
  policy_arn = var.data_query_access_policy_id
}

resource aws_batch_compute_environment batch_compute_enviroment {
  compute_environment_name = "ukho-batch-compute-enviroment"
  type                     = "MANAGED"

  compute_resources {
    type          = "EC2"
    instance_role = aws_iam_instance_profile.ecs_instance_profile.arn

    instance_type = [
      "optimal",
    ]

    max_vcpus = 256
    min_vcpus = 0

    security_group_ids = var.network_security_group_ids
    subnets            = var.network_subnet_ids
  }

  service_role = aws_iam_role.aws_batch_service_role.arn
  depends_on   = [aws_iam_role_policy_attachment.aws_batch_service_role]
}
