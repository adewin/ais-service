resource aws_iam_group data_query_access_group {
  name = "${var.resource_prefix}AccessUsers"
}

resource aws_iam_group_policy data_query_access_permissions {
  name   = "${var.resource_prefix}Permissions"
  group  = aws_iam_group.data_query_access_group.name
  policy = <<EOF
{
    "Version": "2012-10-17",
    "Statement": [{
           "Sid": "GluePermissions",
           "Effect": "Allow",
           "Action": [
               "glue:GetDatabase",
               "glue:GetPartition",
               "glue:UpdateDatabase",
               "glue:GetTableVersion",
               "glue:CreateTable",
               "glue:GetTables",
               "glue:GetTableVersions",
               "glue:GetPartitions",
               "glue:UpdateTable",
               "glue:CreatePartition",
               "glue:GetDatabases",
               "glue:GetTable"
           ],
           "Resource": "*"
       }, {
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
       }, {
         "Sid": "S3ListPermissions",
         "Effect": "Allow",
         "Action": [
            "s3:ListAllMyBuckets"
         ],
         "Resource": "*"
       }, {
         "Sid": "S3ResultsStorePermissions",
         "Effect": "Allow",
         "Action": [
            "s3:PutObject*",
            "s3:GetObject*",
            "s3:List*"
         ],
         "Resource": [
            "${var.results_store_id}",
            "${var.results_store_id}/*"
         ]
       }]
}
EOF
}
