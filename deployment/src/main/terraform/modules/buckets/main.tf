resource "aws_s3_bucket" "jobs_bucket" {
  bucket = "${var.jobs_bucket}"
  acl    = "private"
}

resource "aws_s3_bucket" "ais_bucket" {
  bucket = "${var.ais_bucket}"
  acl    = "private"
}

resource "aws_iam_group" "ais_full_data_access" {
  name = "AISFullAccessUsers"
}

resource "aws_iam_group_policy" "ais_full_data_access" {
  name   = "ais_full_access"
  group  = "${aws_iam_group.ais_full_data_access.id}"
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

resource "aws_iam_group" "ais_read_only_data_access" {
  name = "AISReadOnlyAccessUsers"
}

resource "aws_iam_group_policy" "ais_read_only_data_access" {
  name = "ais_read_only_access"
  group = "${aws_iam_group.ais_read_only_data_access.id}"
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

resource "aws_s3_bucket" "heatmap_bucket" {
  bucket = "${var.heatmap_bucket}"
  acl    = "private"
}

resource "aws_iam_group" "heatmap_read_only_data_access" {
  name = "HeatmapReadOnlyAccessUsers"
}

resource "aws_iam_group_policy" "heatmap_read_only_data_access" {
  name   = "heatmap_read_only_access"
  group  = "${aws_iam_group.heatmap_read_only_data_access.id}"
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
                "${aws_s3_bucket.heatmap_bucket.arn}"
            ]
        },
        {
            "Sid": "ObjectActions",
            "Effect": "Allow",
            "Action": "s3:GetObject*",
            "Resource": [
                "${aws_s3_bucket.heatmap_bucket.arn}/*"
            ]
        }
    ]
}
EOF
}

resource "aws_s3_bucket" "emr_logs_bucket" {
  bucket = "${var.emr_logs_bucket}"
  acl = "private"
}

resource "aws_ssm_parameter" "jobs_bucket" {
  name = "/buckets/jobs_bucket"
  type = "SecureString"
  value = "${var.jobs_bucket}"
}

resource "aws_ssm_parameter" "ais_bucket" {
  name = "/buckets/ais_bucket"
  type = "SecureString"
  value = "${var.ais_bucket}"
}

resource "aws_ssm_parameter" "heatmap_bucket" {
  name = "/buckets/heatmap_bucket"
  type = "SecureString"
  value = "${var.heatmap_bucket}"
}

resource "aws_ssm_parameter" "emr_logs_bucket" {
  name = "/buckets/emr_logs_bucket"
  type = "SecureString"
  value = "${var.emr_logs_bucket}"
}

resource "aws_s3_bucket_object" "spark_s3_job" {
  bucket = "${aws_s3_bucket.jobs_bucket.bucket}"
  key = "${var.spark_job_jar_name}"
  source = "${var.spark_job_jar_path}"
}
