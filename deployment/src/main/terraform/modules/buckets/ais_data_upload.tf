resource aws_s3_bucket ais_data_upload_bucket {
  bucket = var.ais_data_upload_bucket
  acl    = "private"
}

resource aws_iam_group ais_data_upload_full_data_access {
  name = "AISDataUploadFullAccessUsers"
}

resource aws_iam_group_policy ais_data_upload_full_data_access {
  name   = "ais_data_upload_full_access"
  group  = aws_iam_group.ais_data_upload_full_data_access.id
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
                "${aws_s3_bucket.ais_data_upload_bucket.arn}"
            ]
        },
        {
            "Sid": "AllObjectActions",
            "Effect": "Allow",
            "Action": "s3:*Object",
            "Resource": [
                "${aws_s3_bucket.ais_data_upload_bucket.arn}/*"
            ]
        }
    ]
}
EOF
}

resource aws_iam_group ais_data_upload_read_only_data_access {
  name = "AISDataUploadReadOnlyAccessUsers"
}

resource aws_iam_group_policy ais_data_upload_read_only_data_access {
  name = "ais_data_upload_read_only_access"
  group = aws_iam_group.ais_data_upload_read_only_data_access.id
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
                "${aws_s3_bucket.ais_data_upload_bucket.arn}"
            ]
        },
        {
            "Sid": "ObjectActions",
            "Effect": "Allow",
            "Action": "s3:GetObject*",
            "Resource": [
                "${aws_s3_bucket.ais_data_upload_bucket.arn}/*"
            ]
        }
    ]
}
EOF
}

resource aws_ssm_parameter ais_data_upload_bucket {
  name  = "/buckets/ais_data_upload_bucket"
  type  = "SecureString"
  value = var.ais_data_upload_bucket
}
