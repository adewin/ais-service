resource aws_iam_group read_only_data_access_group {
  name = "${var.resource_prefix}ReadOnlyAccessUsers"
}

resource aws_iam_group_policy store_read_only_data_access {
  name   = "${var.resource_prefix}ReadOnlyAccess"
  group  = aws_iam_group.read_only_data_access_group.id
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
            "Sid": "ObjectActions",
            "Effect": "Allow",
            "Action": "s3:GetObject*",
            "Resource": [
                "${var.store_id}/*"
            ]
        }
    ]
}
EOF
}
