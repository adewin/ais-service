package uk.gov.ukho.ais.rasters

import geotrellis.util.Haversine
import org.apache.spark.rdd.RDD

import scala.collection.mutable

object Resampler {

  private final val TIME_STEP: Long = 3 * 60 * 1000

  implicit class RDDResampler(rdd: RDD[(String, ShipPing)]) {
    def resample(implicit config: Config): RDD[ShipPing] = {
      rdd
        .groupByKey()
        .flatMap {
          case (_: String, pings: Seq[ShipPing]) =>
            Resampler.resamplePings(pings)
        }
    }
  }

  private def resamplePings(pings: Seq[ShipPing])(
      implicit config: Config): Seq[ShipPing] = {
    val outputPings: mutable.MutableList[ShipPing] = mutable.MutableList()
    var prevPing: ShipPing = null

    pings
      .sortBy {
        case ShipPing(_: String,
                      acquisitionTime: Long,
                      _: Double,
                      _: Double,
                      _: Double,
                      _: Int) =>
          acquisitionTime
      }
      .foreach(currPing => {

        if (prevPing == null) {
          outputPings += currPing
        } else {
          outputPings ++= interpolateBetweenPingsFromTime(
            prevPing,
            currPing,
            outputPings.last.acquisitionTime)
        }

        prevPing = currPing
      })

    outputPings
  }

  private def interpolateBetweenPingsFromTime(
      prevPing: ShipPing,
      currPing: ShipPing,
      fromTime: Long)(implicit config: Config) =
    if (shouldInterpolateBetweenPings(prevPing, currPing)) {
      addInterpolatedPings(prevPing, currPing, fromTime)
    } else {
      List(currPing)
    }

  private def shouldInterpolateBetweenPings(
      prevPing: ShipPing,
      currPing: ShipPing)(implicit config: Config) = {
    isTimeBetweenPingsWithinThreshold(
      prevPing,
      currPing,
      config.interpolationTimeThresholdMilliseconds) &&
    isDistanceBetweenPingsWithinThreshold(
      prevPing,
      currPing,
      config.interpolationDistanceThresholdMeters)
  }

  private def isDistanceBetweenPingsWithinThreshold(
      prevPing: ShipPing,
      currPing: ShipPing,
      distanceThreshold: Long): Boolean = {
    Haversine(prevPing.longitude,
              prevPing.latitude,
              currPing.longitude,
              currPing.latitude) <= distanceThreshold
  }

  private def isTimeBetweenPingsWithinThreshold(
      prevPing: ShipPing,
      currPing: ShipPing,
      timeThreshold: Long): Boolean = {
    currPing.acquisitionTime - prevPing.acquisitionTime <= timeThreshold
  }

  private def addInterpolatedPings(prevPing: ShipPing,
                                   currPing: ShipPing,
                                   fromTime: Long): List[ShipPing] = {
    var nextTime: Long = fromTime + TIME_STEP
    val interpolatedPings = mutable.MutableList[ShipPing]()

    while (nextTime <= currPing.acquisitionTime) {
      interpolatedPings += interpolateForNextTime(nextTime, prevPing, currPing)
      nextTime += TIME_STEP
    }

    interpolatedPings.toList
  }

  private def interpolateForNextTime(nextTime: Long,
                                     prevPing: ShipPing,
                                     currPing: ShipPing): ShipPing = {
    val timeProportion
      : Double = (nextTime - prevPing.acquisitionTime).toDouble /
      (currPing.acquisitionTime - prevPing.acquisitionTime).toDouble

    val latGap: Double = currPing.latitude - prevPing.latitude
    val longGap: Double = currPing.longitude - prevPing.longitude

    val newLat: Double = timeProportion * latGap + prevPing.latitude
    val newLong: Double = timeProportion * longGap + prevPing.longitude

    ShipPing(currPing.mmsi,
             nextTime,
             newLat,
             newLong,
             currPing.draught,
             currPing.messageTypeId)
  }
}
