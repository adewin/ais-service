package uk.gov.ukho.ais.resample

import java.sql.Timestamp
import java.time.Instant

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
      .sortBy(shipPing => shipPing.acquisitionTime.getTime)
      .foreach(currPing => {

        if (prevPing == null) {
          outputPings += currPing
        } else {
          outputPings ++= interpolateBetweenPingsFromTime(
            prevPing,
            currPing,
            outputPings.last.acquisitionTime.getTime)
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
    currPing.acquisitionTime.getTime - prevPing.acquisitionTime.getTime <= timeThreshold
  }

  private def addInterpolatedPings(prevPing: ShipPing,
                                   currPing: ShipPing,
                                   fromTime: Long): List[ShipPing] = {
    var nextTime: Long = fromTime + TIME_STEP
    val interpolatedPings = mutable.MutableList[ShipPing]()

    while (nextTime < currPing.acquisitionTime.getTime) {
      interpolatedPings += interpolateForNextTime(nextTime, prevPing, currPing)
      nextTime += TIME_STEP
    }

    if (nextTime == currPing.acquisitionTime.getTime) {
      interpolatedPings += currPing
    }

    interpolatedPings.toList
  }

  private def interpolateForNextTime(nextTimeMillis: Long,
                                     prevPing: ShipPing,
                                     currPing: ShipPing): ShipPing = {
    val timeProportion
      : Double = (nextTimeMillis - prevPing.acquisitionTime.getTime).toDouble /
      (currPing.acquisitionTime.getTime - prevPing.acquisitionTime.getTime).toDouble

    val latGap: Double = currPing.latitude - prevPing.latitude
    val longGap: Double = currPing.longitude - prevPing.longitude

    val newLat: Double = timeProportion * latGap + prevPing.latitude
    val newLong: Double = timeProportion * longGap + prevPing.longitude

    prevPing.copy(longitude = newLong,
                  latitude = newLat,
                  acquisitionTime =
                    Timestamp.from(Instant.ofEpochMilli(nextTimeMillis)))
  }
}
