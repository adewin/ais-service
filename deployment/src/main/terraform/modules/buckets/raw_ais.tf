resource aws_s3_bucket ais_bucket {
  bucket = var.raw_ais_bucket
  acl    = "private"
}

resource aws_iam_group ais_full_data_access {
  name = "AISFullAccessUsers"
}

resource aws_iam_group_policy ais_full_data_access {
  name   = "ais_full_access"
  group  = aws_iam_group.ais_full_data_access.id
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
                "${aws_s3_bucket.ais_bucket.arn}"
            ]
        },
        {
            "Sid": "AllObjectActions",
            "Effect": "Allow",
            "Action": "s3:*Object",
            "Resource": [
                "${aws_s3_bucket.ais_bucket.arn}/*"
            ]
        }
    ]
}
EOF
}

resource aws_iam_group ais_read_only_data_access {
  name = "AISReadOnlyAccessUsers"
}

resource aws_iam_group_policy ais_read_only_data_access {
  name = "ais_read_only_access"
  group = aws_iam_group.ais_read_only_data_access.id
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
                "${aws_s3_bucket.ais_bucket.arn}"
            ]
        },
        {
            "Sid": "ObjectActions",
            "Effect": "Allow",
            "Action": "s3:GetObject*",
            "Resource": [
                "${aws_s3_bucket.ais_bucket.arn}/*"
            ]
        }
    ]
}
EOF
}

resource aws_ssm_parameter ais_bucket {
  name  = "/buckets/ais_bucket"
  type  = "SecureString"
  value = var.raw_ais_bucket
}
