package uk.gov.ukho.ais

import org.apache.spark.sql.types._

object Schema {

  val ARKEVISTA_POS_ID = "ArkPosId"
  val MMSI = "MMSI"
  val ACQUISITION_TIME = "acquisition_time"
  val LONGITUDE = "lon"
  val LATITUDE = "lat"
  val VESSEL_CLASS = "vessel_class"
  val MESSAGE_TYPE_ID = "message_type_id"
  val NAVIGATIONAL_STATUS = "navigational_status"
  val RATE_OF_TURN = "rot"
  val SPEED_OVER_GROUND = "sog"
  val COURSE_OVER_GROUND = "cog"
  val TRUE_HEADING = "true_heading"
  val ALTITUDE = "altitude"
  val SPECIAL_MANEUVER = "special_maneuver"
  val RADIO_STATUS = "radio_status"
  val FLAGS = "flags"
  val INPUT_AIS_DATA_FILE = "input_ais_data_file"
  val YEAR = "year"
  val MONTH = "month"
  val DAY = "day"
  val ARKEVISTA_STATIC_ID = "ArkStaticId"
  val CALL_SIGN = "call_sign"
  val IMO_NUMBER = "IMO_number"
  val VESSEL_NAME = "vessel_name"
  val CARGO_TYPE_INDEX = "cargo_type_indx"
  val ACTIVITY_TYPE_INDEX = "activity_type_indx"
  val SHIP_TYPE = "ship_type"
  val BOW = "bow"
  val STERN = "stern"
  val PORT = "port"
  val STARBOARD = "starboard"
  val DRAUGHT = "draught"
  val AIS_VERSION = "AIS_version"
  val POSITION_DEVICE = "position_device"
  val ETA = "ETA"
  val DESTINATION = "destination"
  val DTE = "DTE"
  val LAST_TIME_PREV_VAL_SEQ = "LastTimePrevValSeq"
  val FIRST_TIME_THIS_VAL_SEQ = "FirstTimeThisValSeq"
  val LAST_TIME_THIS_VAL_SEQ = "LastTimeThisValSeq"
  val FIRST_TIME_NEXT_VAL_SEQ = "FirstTimeNextValSeq"
  val COUNT_SEQ = "count_seq"
  val VESSEL_TYPE_INDEX = "vessel_type_indx"

  val AIS_SCHEMA: StructType = StructType(
    Array(
      StructField(ARKEVISTA_POS_ID, StringType, nullable = false),
      StructField(MMSI, StringType, nullable = false),
      StructField(ACQUISITION_TIME, TimestampType, nullable = false),
      StructField(LONGITUDE, DoubleType, nullable = false),
      StructField(LATITUDE, DoubleType, nullable = false),
      StructField(VESSEL_CLASS, StringType, nullable = false),
      StructField(MESSAGE_TYPE_ID, IntegerType, nullable = false),
      StructField(NAVIGATIONAL_STATUS, StringType, nullable = false),
      StructField(RATE_OF_TURN, StringType, nullable = false),
      StructField(SPEED_OVER_GROUND, StringType, nullable = false),
      StructField(COURSE_OVER_GROUND, StringType, nullable = false),
      StructField(TRUE_HEADING, StringType, nullable = false),
      StructField(ALTITUDE, StringType, nullable = false),
      StructField(SPECIAL_MANEUVER, StringType, nullable = false),
      StructField(RADIO_STATUS, StringType, nullable = false),
      StructField(FLAGS, StringType, nullable = false)
    ))

  val PARTITIONED_AIS_SCHEMA: StructType =
    AIS_SCHEMA
      .add(INPUT_AIS_DATA_FILE, StringType, nullable = false)
      .add(YEAR, IntegerType, nullable = false)
      .add(MONTH, IntegerType, nullable = false)
      .add(DAY, IntegerType, nullable = false)

  val STATIC_DATA_SCHEMA: StructType = StructType(
    Array(
      StructField(ARKEVISTA_STATIC_ID, StringType, nullable = false),
      StructField(MMSI, StringType, nullable = false),
      StructField(CALL_SIGN, StringType, nullable = false),
      StructField(IMO_NUMBER, IntegerType, nullable = false),
      StructField(VESSEL_NAME, StringType, nullable = false),
      StructField(CARGO_TYPE_INDEX, IntegerType, nullable = false),
      StructField(ACTIVITY_TYPE_INDEX, IntegerType, nullable = false),
      StructField(SHIP_TYPE, IntegerType, nullable = false),
      StructField(BOW, DoubleType, nullable = false),
      StructField(STERN, DoubleType, nullable = false),
      StructField(PORT, DoubleType, nullable = false),
      StructField(STARBOARD, DoubleType, nullable = false),
      StructField(DRAUGHT, DoubleType, nullable = false),
      StructField(VESSEL_TYPE_INDEX, IntegerType, nullable = false),
      StructField(MESSAGE_TYPE_ID, IntegerType, nullable = false),
      StructField(AIS_VERSION, IntegerType, nullable = false),
      StructField(POSITION_DEVICE, DoubleType, nullable = false),
      StructField(ETA, TimestampType, nullable = false),
      StructField(DESTINATION, StringType, nullable = false),
      StructField(DTE, IntegerType, nullable = false),
      StructField(LAST_TIME_PREV_VAL_SEQ, TimestampType, nullable = false),
      StructField(FIRST_TIME_THIS_VAL_SEQ, TimestampType, nullable = false),
      StructField(LAST_TIME_THIS_VAL_SEQ, TimestampType, nullable = false),
      StructField(FIRST_TIME_NEXT_VAL_SEQ, TimestampType, nullable = false),
      StructField(COUNT_SEQ, IntegerType, nullable = true)
    ))

  val DRAUGHT_SCHEMA: StructType = StructType(
    Array(
      StructField("MinPoint", DoubleType, nullable = false),
      StructField("MaxPoint", DoubleType, nullable = false)
    ))
}
