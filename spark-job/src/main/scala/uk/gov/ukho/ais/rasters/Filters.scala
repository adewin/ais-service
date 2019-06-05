package uk.gov.ukho.ais.rasters

import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row

object AisMessageTypes extends Enumeration {
  val CLASS_A_POSITION_TYPES = List(1, 2, 3)
  val CLASS_B_POSITION_TYPES = List(18, 19)
}

object Filters {
  val VALID_MESSAGE_TYPES: List[Int] = List(
    AisMessageTypes.CLASS_A_POSITION_TYPES,
    AisMessageTypes.CLASS_B_POSITION_TYPES
  ).flatten

  implicit class RDDFilters(rdd: RDD[Row]) {

    def filterByValidMessageType(): RDD[Row] = {
      rdd.filter {
        case Row(_: Double, _: Double, msgType: Int) =>
          VALID_MESSAGE_TYPES.contains(msgType)
      }
    }
  }
}
