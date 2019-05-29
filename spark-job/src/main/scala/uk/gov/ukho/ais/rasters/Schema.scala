package uk.gov.ukho.ais.rasters

import org.apache.spark.sql.types.{
  DoubleType,
  IntegerType,
  StringType,
  StructField,
  StructType,
  TimestampType
}

object Schema {
  val AIS_SCHEMA = StructType(
    Array(
      StructField("ArkPosId", StringType, nullable = false),
      StructField("MMSI", StringType, nullable = false),
      StructField("acquisition_time", TimestampType, nullable = false),
      StructField("lon", DoubleType, nullable = false),
      StructField("lat", DoubleType, nullable = false),
      StructField("vessel_class", StringType, nullable = false),
      StructField("message_type_id", IntegerType, nullable = false),
      StructField("navigational_status", StringType, nullable = false),
      StructField("rot", StringType, nullable = false),
      StructField("sog", StringType, nullable = false),
      StructField("cog", StringType, nullable = false),
      StructField("true_heading", StringType, nullable = false),
      StructField("altitude", StringType, nullable = false),
      StructField("special_manoeurve", StringType, nullable = false),
      StructField("radio_status", StringType, nullable = false),
      StructField("flags", StringType, nullable = false)
    ))
}
