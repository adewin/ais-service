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

resource aws_iam_instance_profile ecs_instance_role {
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

resource aws_iam_policy athena_policy {
  name = "athena_batch_policy"

  policy = <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Sid": "AthenaPermissions",
       "Effect": "Allow",
       "Action": [
         "athena:StartQueryExecution",
         "athena:CreateNamedQuery",
         "athena:GetQueryResultsStream",
         "athena:StopQueryExecution",
         "athena:GetQueryExecution",
         "athena:GetQueryResults",
         "athena:BatchGetNamedQuery",
         "athena:GetNamedQuery",
         "athena:ListTagsForResource",
         "athena:BatchGetQueryExecution",
         "athena:ListQueryExecutions",
         "athena:ListNamedQueries"
       ],
       "Resource": "*"
    }
  ]
}
EOF
}

resource aws_iam_role_policy_attachment athena_policy_attach {
  role = aws_iam_role.ecs_instance_role.name
  policy_arn = aws_iam_policy.athena_policy.arn
}


resource aws_iam_role_policy_attachment aws_batch_service_role {
  role = aws_iam_role.aws_batch_service_role.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AWSBatchServiceRole"
}

resource aws_batch_compute_environment ukho-batch-compute-enviroment {
  compute_environment_name = "ukho-batch-compute-enviroment"

  compute_resources {
    instance_role = aws_iam_instance_profile.ecs_instance_role.arn

    instance_type = [
      "optimal",
    ]

    max_vcpus = 256
    min_vcpus = 0

    security_group_ids = var.network_security_group_ids

    subnets = var.network_subnet_ids

    type = "EC2"
  }

  service_role = aws_iam_role.aws_batch_service_role.arn
  type = "MANAGED"

  depends_on = [
    aws_iam_role_policy_attachment.aws_batch_service_role]
}
