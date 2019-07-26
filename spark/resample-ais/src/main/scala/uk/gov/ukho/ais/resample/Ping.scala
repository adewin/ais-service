package uk.gov.ukho.ais.resample

import java.sql.Timestamp

import org.apache.spark.sql.Row
import uk.gov.ukho.ais.Schema._
import uk.gov.ukho.ais.SchemaTuple.PartitionedAisSchema

case class Ping(id: String,
                mmsi: String,
                acquisitionTime: Timestamp,
                longitude: Double,
                latitude: Double,
                row: Row) {

  def asTuple(): PartitionedAisSchema =
    (this.id,
     this.mmsi,
     this.acquisitionTime,
     this.longitude,
     this.latitude,
     this.row.getAs[String](VESSEL_CLASS),
     this.row.getAs[Int](MESSAGE_TYPE_ID),
     this.row.getAs[String](NAVIGATIONAL_STATUS),
     this.row.getAs[String](RATE_OF_TURN),
     this.row.getAs[String](SPEED_OVER_GROUND),
     this.row.getAs[String](COURSE_OVER_GROUND),
     this.row.getAs[String](TRUE_HEADING),
     this.row.getAs[String](ALTITUDE),
     this.row.getAs[String](SPECIAL_MANEUVER),
     this.row.getAs[String](RADIO_STATUS),
     this.row.getAs[String](FLAGS),
     this.row.getAs[String](INPUT_AIS_DATA_FILE),
     this.row.getAs[Int](YEAR),
     this.row.getAs[Int](MONTH),
     this.row.getAs[Int](DAY))
}
