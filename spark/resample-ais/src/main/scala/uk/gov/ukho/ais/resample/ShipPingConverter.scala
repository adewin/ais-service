package uk.gov.ukho.ais.resample

import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row

object ShipPingConverter {

  implicit class ConvertToShipPingTuple(rdd: RDD[Row]) {

    def convertToKeyedTuple(): RDD[(String, ShipPing)] = {
      rdd.map((row: Row) => (row.getAs[String](1), ShipPing.fromRow(row)))
    }
  }
}
