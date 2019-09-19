package uk.gov.ukho.ais.heatmaps.generator.processor

import java.sql.Timestamp

import geotrellis.util.Haversine
import uk.gov.ukho.ais.heatmaps.generator.Config
import uk.gov.ukho.ais.heatmaps.generator.model.Ping

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

object Resampler {

  private final val TIME_STEP: Long = 3 * 60 * 1000

  def resamplePings(pings: Iterator[Ping])(
      implicit config: Config): Iterator[Ping] = new Iterator[Ping] {
    var prevPing: Ping = _
    var lastAcquisitionTime: Long = _
    var outputQueue: mutable.Queue[Ping] = new mutable.Queue[Ping]()
    var pingCount: Long = 0

    override def hasNext: Boolean = {
      while (outputQueue.isEmpty && pings.hasNext) {
        val currPing = pings.next()

        if (prevPing == null) {
          outputQueue += currPing
        } else {
          outputQueue ++= interpolateBetweenPingsFromTime(
            prevPing,
            currPing,
            lastAcquisitionTime
          )
        }

        prevPing = currPing
      }

      outputQueue.nonEmpty
    }

    override def next(): Ping = {
      lastAcquisitionTime = outputQueue.last.acquisitionTime.getTime
      outputQueue.dequeue()
    }
  }

  private def interpolateBetweenPingsFromTime(
      prevPing: Ping,
      currPing: Ping,
      fromTime: Long)(implicit config: Config): ArrayBuffer[Ping] =
    if (shouldInterpolateBetweenPings(prevPing, currPing)) {
      addInterpolatedPings(prevPing, currPing, fromTime)
    } else {
      ArrayBuffer(currPing)
    }

  private def shouldInterpolateBetweenPings(prevPing: Ping, currPing: Ping)(
      implicit config: Config): Boolean = {
    isTimeBetweenPingsWithinThreshold(
      prevPing,
      currPing,
      config.interpolationTimeThresholdMilliseconds) &&
    isDistanceBetweenPingsWithinThreshold(
      prevPing,
      currPing,
      config.interpolationDistanceThresholdMeters) &&
    prevPing.mmsi == currPing.mmsi
  }

  private def isDistanceBetweenPingsWithinThreshold(
      prevPing: Ping,
      currPing: Ping,
      distanceThreshold: Long): Boolean = {
    Haversine(prevPing.longitude,
              prevPing.latitude,
              currPing.longitude,
              currPing.latitude) <= distanceThreshold
  }

  private def isTimeBetweenPingsWithinThreshold(
      prevPing: Ping,
      currPing: Ping,
      timeThreshold: Long): Boolean = {
    currPing.acquisitionTime.getTime - prevPing.acquisitionTime.getTime <= timeThreshold
  }

  private def addInterpolatedPings(prevPing: Ping,
                                   currPing: Ping,
                                   fromTime: Long): ArrayBuffer[Ping] = {
    var nextTime: Long = fromTime + TIME_STEP
    val interpolatedPings = mutable.ArrayBuffer[Ping]()

    while (nextTime < currPing.acquisitionTime.getTime) {
      interpolatedPings += interpolateForNextTime(nextTime, prevPing, currPing)
      nextTime += TIME_STEP
    }

    if (nextTime == currPing.acquisitionTime.getTime) {
      interpolatedPings += currPing
    }

    interpolatedPings
  }

  private def interpolateForNextTime(nextTime: Long,
                                     prevPing: Ping,
                                     currPing: Ping): Ping = {
    val timeProportion
      : Double = (nextTime - prevPing.acquisitionTime.getTime).toDouble /
      (currPing.acquisitionTime.getTime - prevPing.acquisitionTime.getTime).toDouble

    val latGap: Double = currPing.latitude - prevPing.latitude
    val lonGap: Double = currPing.longitude - prevPing.longitude

    val newLat: Double = timeProportion * latGap + prevPing.latitude
    val newLon: Double = timeProportion * lonGap + prevPing.longitude

    Ping(currPing.mmsi, new Timestamp(nextTime), newLon, newLat)
  }
}
