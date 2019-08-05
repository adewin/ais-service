resource aws_glue_catalog_table processed_static_data_store {
  name          = var.processed_static_data_store_name
  database_name = var.ais_database_name

  table_type = "EXTERNAL_TABLE"

  parameters = {
    EXTERNAL = "TRUE"
  }

  storage_descriptor {
    location      = "s3://${var.processed_static_data_store_name}/"
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
      name = "callsign"
      type = "string"
    }

    columns {
      name = "imo"
      type = "int"
    }

    columns {
      name = "vessel_name"
      type = "string"
    }

    columns {
      name = "cargo_type_index"
      type = "int"
    }

    columns {
      name = "activity_type_index"
      type = "int"
    }

    columns {
      name = "ship_type"
      type = "int"
    }

    columns {
      name = "bow"
      type = "double"
    }

    columns {
      name = "stern"
      type = "double"
    }

    columns {
      name = "port"
      type = "double"
    }

    columns {
      name = "starboard"
      type = "double"
    }

    columns {
      name = "draught"
      type = "double"
    }

    columns {
      name = "vessel_type_index"
      type = "double"
    }

    columns {
      name = "message_type_id"
      type = "int"
    }

    columns {
      name = "ais_version"
      type = "int"
    }

    columns {
      name = "position_device"
      type = "double"
    }

    columns {
      name = "eta"
      type = "timestamp"
    }

    columns {
      name = "destination"
      type = "string"
    }

    columns {
      name = "dte"
      type = "int"
    }

    columns {
      name = "last_time_prev_val_seq"
      type = "timestamp"
    }

    columns {
      name = "first_time_this_val_seq"
      type = "timestamp"
    }

    columns {
      name = "last_time_this_val_seq"
      type = "timestamp"
    }

    columns {
      name = "first_time_next_val_seq"
      type = "timestamp"
    }

    columns {
      name = "count_seq"
      type = "int"
    }
  }
}
