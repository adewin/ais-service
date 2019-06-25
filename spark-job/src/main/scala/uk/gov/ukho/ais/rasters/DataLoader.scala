package uk.gov.ukho.ais.rasters

import java.sql.Timestamp

import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row

object DataLoader {

  implicit class MapRDDData(rdd: RDD[Row]) {

    def mapStaticDataToKeyedTuple(): RDD[(String, Double)] = {
      rdd.map {
        case Row(mmsi: String, draught: Double) => (mmsi, draught)
      }
    }

    def mapAisDataToKeyedTuple(): RDD[(String, (Long, Double, Double, Int))] = {
      rdd.map {
        case Row(mmsi: String,
                 timestamp: Timestamp,
                 lat: Double,
                 lon: Double,
                 messageTypeId: Int) =>
          (mmsi, (timestamp.getTime, lat, lon, messageTypeId))
      }
    }
  }

  implicit class MapJoinedData(
      rdd: RDD[(String, ((Long, Double, Double, Int), Option[Double]))]) {

    def mapToKeyedShipPingTuple(): RDD[(String, ShipPing)] = {
      rdd.map {
        case (mmsi: String,
              ((timestamp: Long, lat: Double, lon: Double, messageTypeId: Int),
               draught: Option[Double])) =>
          (mmsi,
           ShipPing(mmsi,
                    timestamp,
                    lat,
                    lon,
                    draught.fold(-1D)(d => d),
                    messageTypeId))
      }
    }
  }

  def loadAisData(): RDD[(String, ShipPing)] = {
    val staticData = Session.sparkSession.read
      .schema(Schema.STATIC_DATA_SCHEMA)
      .option("sep", "\t")
      .csv(ConfigParser.config.staticDataFile)
      .select("MMSI", "draught")
      .groupBy("MMSI")
      .max("draught")
      .rdd
      .mapStaticDataToKeyedTuple()

    Session.sparkSession.read
      .schema(Schema.AIS_SCHEMA)
      .option("sep", "\t")
      .csv(ConfigParser.config.inputPath)
      .select("MMSI", "acquisition_time", "lat", "lon", "message_type_id")
      .rdd
      .mapAisDataToKeyedTuple()
      .leftOuterJoin(staticData)
      .mapToKeyedShipPingTuple()
  }

  def loadVesselDraughtRangeData(): Array[VesselDraughtRange] = {
    Session.sparkSession.read
      .schema(Schema.DRAUGHT_SCHEMA)
      .option("sep", "\t")
      .csv(ConfigParser.config.draughtConfigFile)
      .select("MinPoint", "MaxPoint")
      .rdd
      .map {
        case Row(minPoint: Double, maxPoint: Double) =>
          VesselDraughtRange(minPoint, maxPoint)
      }
      .collect()
  }
}
