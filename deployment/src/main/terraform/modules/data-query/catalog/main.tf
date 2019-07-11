resource aws_glue_catalog_database catalog_database {
  name = var.catalog_database_name
}

resource aws_glue_catalog_table catalog_database_table {
  name          = var.catalog_database_table_name
  database_name = aws_glue_catalog_database.catalog_database.name

  table_type = "EXTERNAL_TABLE"

  parameters = {
    EXTERNAL = "TRUE"
  }

  partition_keys {
    name = "year"
    type = "int"
  }

  partition_keys {
    name = "month"
    type = "int"
  }

  storage_descriptor {
    location      = "s3://${var.data_store_name}/${var.data_prefix}"
    input_format  = "org.apache.hadoop.mapred.TextInputFormat"
    output_format = "org.apache.hadoop.hive.ql.io.HiveIgnoreKeyTextOutputFormat"

    ser_de_info {
      name                  = "csv"
      serialization_library = "org.apache.hadoop.hive.serde2.lazy.LazySimpleSerDe"

      parameters = {
        "field.delim"          = "\t"
        "mapkey.delim"         = "\u0003"
        "collection.delim"     = "\u0002"
        "serialization.format" = "\t"
      }
    }

    columns {
      name = "arkposid"
      type = "string"
    }

    columns {
      name = "mmsi"
      type = "string"
    }

    columns {
      name = "acquisition_time"
      type = "timestamp"
    }

    columns {
      name = "lon"
      type = "double"
    }

    columns {
      name = "lat"
      type = "double"
    }

    columns {
      name = "vessel_class"
      type = "string"
    }

    columns {
      name = "message_type_id"
      type = "int"
    }

    columns {
      name = "navigational_status"
      type = "string"
    }

    columns {
      name = "rot"
      type = "string"
    }

    columns {
      name = "sog"
      type = "string"
    }

    columns {
      name = "cog"
      type = "string"
    }

    columns {
      name = "true_heading"
      type = "string"
    }

    columns {
      name = "altitude"
      type = "string"
    }

    columns {
      name = "special_manoeurve"
      type = "string"
    }

    columns {
      name = "radio_status"
      type = "string"
    }

    columns {
      name = "flags"
      type = "string"
    }
  }
}

resource aws_iam_role catalog_database_crawler_execution_role {
  name               = "${var.catalog_database_name}CrawlerRole"
  assume_role_policy = <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Action": "sts:AssumeRole",
      "Principal": {
        "Service": "glue.amazonaws.com"
      },
      "Effect": "Allow",
      "Sid": ""
    }
  ]
}
EOF
}

resource aws_iam_policy catalog_database_crawler_policy {
  name_prefix = "${var.catalog_database_name}CrawlerS3"
  policy = <<EOF
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Sid": "ConsoleAccess",
            "Effect": "Allow",
            "Action": [
                "s3:ListAllMyBuckets"
            ],
            "Resource": "*"
        },
        {
            "Sid": "ListObjectsInBucket",
            "Effect": "Allow",
            "Action": "s3:List*",
            "Resource": [
                "arn:aws:s3:::${var.data_store_name}"
            ]
        },
        {
            "Sid": "ObjectActions",
            "Effect": "Allow",
            "Action": "s3:GetObject*",
            "Resource": [
                "arn:aws:s3:::${var.data_store_name}/*"
            ]
        }
    ]
}
EOF
}

resource aws_iam_role_policy_attachment catalog_database_crawler_policy_attachment {
  policy_arn = aws_iam_policy.catalog_database_crawler_policy.arn
  role       = aws_iam_role.catalog_database_crawler_execution_role.name
}

resource aws_glue_crawler catalog_database_crawler {
  database_name = aws_glue_catalog_database.catalog_database.name
  name          = "${aws_glue_catalog_database.catalog_database.name}-crawler"
  role          = aws_iam_role.catalog_database_crawler_execution_role.arn

  s3_target {
    path = "s3://${var.data_store_name}/${var.data_prefix}"
  }
}
