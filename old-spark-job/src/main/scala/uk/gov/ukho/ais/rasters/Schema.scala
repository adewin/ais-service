package uk.gov.ukho.ais.rasters

import org.apache.spark.sql.types._

object Schema {
  val STATIC_DATA_SCHEMA = StructType(
    Array(
      StructField("ArkStaticId", StringType, nullable = false),
      StructField("MMSI", StringType, nullable = false),
      StructField("call_sign", StringType, nullable = false),
      StructField("IMO_number", IntegerType, nullable = false),
      StructField("vessel_name", StringType, nullable = false),
      StructField("cargo_type_indx", IntegerType, nullable = false),
      StructField("activity_type_indx", IntegerType, nullable = false),
      StructField("ship_type", IntegerType, nullable = false),
      StructField("bow", DoubleType, nullable = false),
      StructField("stern", DoubleType, nullable = false),
      StructField("port", DoubleType, nullable = false),
      StructField("starboard", DoubleType, nullable = false),
      StructField("draught", DoubleType, nullable = false),
      StructField("vessel_type_indx", DoubleType, nullable = false)
    ))

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

  val DRAUGHT_SCHEMA = StructType(
    Array(
      StructField("MinPoint", DoubleType, nullable = false),
      StructField("MaxPoint", DoubleType, nullable = false)
    ))
}
