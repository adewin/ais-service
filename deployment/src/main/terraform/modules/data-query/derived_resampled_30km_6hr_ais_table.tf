resource aws_glue_catalog_table derived_resampled_30km_6hr_ais_table {
  name          = var.derived_resampled_30km_6hr_ais_table_name
  database_name = var.ais_database_name

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
    location      = "s3://${var.derived_resampled_ais_store_name}/${var.derived_resampled_30km_6hr_ais_data_prefix}"
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

    columns {
      name = "input_ais_data_file"
      type = "string"
    }
  }
}
