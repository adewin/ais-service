package uk.gov.ukho.ais.rasters

import geotrellis.util.Haversine
import geotrellis.vector.Point

import scala.collection.mutable

@SuppressWarnings(
  Array(
    "org.wartremover.warts.Null",
    "org.wartremover.warts.Var",
    "org.wartremover.warts.TraversableOps"
  ))
object Interpolator {

  private final val TIME_STEP: Long = 3 * 60 * 1000
  private final val TIME_STEP_THRESHOLD: Long = 6 * 60 * 60 * 1000
  private final val DISTANCE_THRESHOLD = 30000

  def interpolatePings(pings: Seq[ShipPing]): Seq[ShipPing] = {

    val outputPings: mutable.MutableList[ShipPing] = mutable.MutableList()
    var prevPing: ShipPing = null

    pings
      .sortBy {
        case ShipPing(_: String, acquisitionTime: Long, _: Double, _: Double) =>
          acquisitionTime
      }
      .foreach(currPing => {

        if (prevPing == null) {
          outputPings += currPing
        } else {
          outputPings ++= interpolateBetweenPingsFromTime(
            prevPing,
            currPing,
            outputPings.last.acquisitionTime
          )
        }

        prevPing = currPing
      })

    outputPings
  }

  private def interpolateBetweenPingsFromTime(prevPing: ShipPing,
                                              currPing: ShipPing,
                                              fromTime: Long): List[ShipPing] =
    if (shouldInterpolateBetweenPings(prevPing, currPing)) {
      addInterpolatedPings(prevPing, currPing, fromTime)
    } else {
      List(currPing)
    }

  private def shouldInterpolateBetweenPings(prevPing: ShipPing,
                                            currPing: ShipPing): Boolean = {
    isTimeBetweenPingsWithinTheshold(prevPing, currPing) &&
    isDistanceBetweenPingsWithinThreshold(prevPing, currPing)
  }

  private def isDistanceBetweenPingsWithinThreshold(
      prevPing: ShipPing,
      currPing: ShipPing): Boolean = {
    Haversine(prevPing.longitude,
              prevPing.latitude,
              currPing.longitude,
              currPing.latitude) <= DISTANCE_THRESHOLD
  }

  private def isTimeBetweenPingsWithinTheshold(prevPing: ShipPing,
                                               currPing: ShipPing): Boolean = {
    currPing.acquisitionTime - prevPing.acquisitionTime <= TIME_STEP_THRESHOLD
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

    ShipPing(currPing.mmsi, nextTime, newLat, newLong)
  }
}
