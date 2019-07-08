resource aws_iam_group data_query_access_group {
  name = "${var.resource_prefix}AccessUsers"
}

resource aws_iam_group_policy data_query_access {
  name   = "${var.resource_prefix}Access"
  group  = aws_iam_group.data_query_access_group.id
  policy = <<EOF
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Sid": "${var.resource_prefix}Access",
            "Effect": "Allow",
            "Action": [
                "athena:*"
            ],
            "Resource": [
                "*"
            ]
        },
        {
          "Sid": "${var.resource_prefix}DenyWorkgroupAccess",
            "Effect": "Deny",
            "Action": [
                "athena:ListWorkGroups",
                "athena:GetQueryExecutions",
                "athena:GetWorkGroup",
                "athena:CreateWorkGroup",
                "athena:DeleteWorkGroup"
            ],
            "Resource": [
                "*"
            ]
        }
    ]
}
EOF
}
