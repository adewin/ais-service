package uk.gov.ukho.ais.resample

import org.apache.spark.rdd.RDD
import org.apache.spark.sql.DataFrame
import uk.gov.ukho.ais.{Schema, Session}

object RDDToDataFrameConverter {

  implicit class Converter(rdd: RDD[ShipPing]) {

    def convertToDataFrame(): DataFrame = {
      val rddAsRows = rdd.map(shipPing => shipPing.toRow)

      Session.sparkSession.sqlContext
        .createDataFrame(rddAsRows, Schema.PARTITIONED_AIS_SCHEMA)
    }
  }

}
