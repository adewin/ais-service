resource aws_iam_group full_data_access_group {
  name = "${var.resource_prefix}FullAccessUsers"
}

resource aws_iam_group_policy store_full_data_access {
  name   = "${var.resource_prefix}FullAccess"
  group  = aws_iam_group.full_data_access_group.id
  policy = <<EOF
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Sid": "ConsoleAccess",
            "Effect": "Allow",
            "Action": [
                "s3:GetAccountPublicAccessBlock",
                "s3:GetBucketAcl",
                "s3:GetBucketLocation",
                "s3:GetBucketPolicyStatus",
                "s3:GetBucketPublicAccessBlock",
                "s3:ListAllMyBuckets"
            ],
            "Resource": "*"
        },
        {
            "Sid": "ListObjectsInBucket",
            "Effect": "Allow",
            "Action": "s3:ListBucket",
            "Resource": [
                "${var.store_id}"
            ]
        },
        {
            "Sid": "AllObjectActions",
            "Effect": "Allow",
            "Action": "s3:*Object",
            "Resource": [
                "${var.store_id}/*"
            ]
        }
    ]
}
EOF
}
