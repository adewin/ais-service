resource aws_s3_bucket sensitive_heatmap_bucket {
  bucket = var.sensitive_heatmap_bucket
  acl    = "private"
}

resource aws_iam_group sensitive_heatmap_read_only_data_access {
  name = "SensitiveHeatmapReadOnlyAccessUsers"
}

resource aws_iam_group_policy sensitive_heatmap_read_only_data_access {
  name   = "sensitive_heatmap_read_only_access"
  group  = aws_iam_group.sensitive_heatmap_read_only_data_access.id
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
                "${aws_s3_bucket.sensitive_heatmap_bucket.arn}"
            ]
        },
        {
            "Sid": "ObjectActions",
            "Effect": "Allow",
            "Action": "s3:GetObject*",
            "Resource": [
                "${aws_s3_bucket.sensitive_heatmap_bucket.arn}/*"
            ]
        }
    ]
}
EOF
}

resource aws_ssm_parameter sensitive_heatmap_bucket {
  name = "/buckets/sensitive_heatmap_bucket"
  type = "SecureString"
  value = var.sensitive_heatmap_bucket
}
