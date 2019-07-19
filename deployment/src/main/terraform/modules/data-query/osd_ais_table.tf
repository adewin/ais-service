resource aws_glue_catalog_table osd_ais_table {
  name          = var.osd_ais_table_name
  database_name = var.database_name

  table_type = "EXTERNAL_TABLE"

  parameters = {
    EXTERNAL = "TRUE"
  }

  storage_descriptor {
    location      = "s3://${var.osd_ais_store_name}/data/"
    input_format  = "org.apache.hadoop.mapred.TextInputFormat"
    output_format = "org.apache.hadoop.hive.ql.io.HiveIgnoreKeyTextOutputFormat"

    ser_de_info {
      name                  = "csv"
      serialization_library = "org.apache.hadoop.hive.serde2.lazy.LazySimpleSerDe"

      parameters = {
        "field.delim" = "\t"
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
      name = "special_manoeuvre"
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
