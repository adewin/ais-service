package uk.gov.ukho.ais.rasters

import java.sql.Timestamp

import org.apache.spark.rdd.RDD

object AisMessageTypes {
  val CLASS_A_POSITION_TYPES = List(1, 2, 3)
  val CLASS_B_POSITION_TYPES = List(18, 19)
}

object AisDataFilters {
  val VALID_MESSAGE_TYPES: List[Int] = List(
    AisMessageTypes.CLASS_A_POSITION_TYPES,
    AisMessageTypes.CLASS_B_POSITION_TYPES
  ).flatten

  implicit class RDDFilters(rdd: RDD[(String, ShipPing)]) {

    def filterShipPings(): RDD[(String, ShipPing)] = {
      val vesselDraughtRanges = DataLoader.loadVesselDraughtRangeData()

      rdd
        .filterByValidMessageType()
        .filterPingsByTimePeriod(ConfigParser.config.startPeriod,
                                 ConfigParser.config.endPeriod)
        .filterPingsByDraught(ConfigParser.config.draughtIndex,
                              vesselDraughtRanges)
    }

    def filterByValidMessageType(): RDD[(String, ShipPing)] = {
      rdd.filter {
        case (_, ping: ShipPing) =>
          VALID_MESSAGE_TYPES.contains(ping.messageTypeId)
      }
    }

    def filterPingsByTimePeriod(
        startPeriod: Timestamp,
        endPeriod: Timestamp): RDD[(String, ShipPing)] = {
      rdd.filter {
        case (_, ping: ShipPing) =>
          ping.acquisitionTime >= startPeriod.getTime && ping.acquisitionTime < endPeriod.getTime
      }
    }

    def filterPingsByDraught(draughtIndex: Option[Int],
                             draughtFilterPoints: Array[VesselDraughtRange])
      : RDD[(String, ShipPing)] =
      draughtIndex.fold {
        rdd
      } {
        case -1 =>
          rdd.filterUnknownShipPings()
        case index: Int =>
          rdd.filterShipPingsInRange(draughtFilterPoints(index))
      }

    def filterUnknownShipPings(): RDD[(String, ShipPing)] = {
      rdd.filter {
        case (_, ping: ShipPing) => ping.draught == 0D || ping.draught == -1D
      }
    }

    def filterShipPingsInRange(
        vesselDraughtRange: VesselDraughtRange): RDD[(String, ShipPing)] = {
      rdd.filter {
        case (_, ping: ShipPing) =>
          vesselDraughtRange.minDraught <= ping.draught && ping.draught < vesselDraughtRange.maxDraught
      }
    }
  }

}
