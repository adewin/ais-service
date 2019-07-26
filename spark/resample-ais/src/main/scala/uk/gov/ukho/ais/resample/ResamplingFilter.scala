package uk.gov.ukho.ais.resample

import java.sql.Timestamp
import java.time.Instant

import geotrellis.util.Haversine
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.expressions.Window
import uk.gov.ukho.ais.Schema._

import scala.collection.mutable

object ResamplingFilter {

  private final val TIME_STEP: Long = 3 * 60 * 1000

  object ResampleAisPings {
    private def interpolateBetweenPingsFromTime(prevPing: Ping, currPing: Ping)(
        implicit config: Config): Seq[Ping] = {
      if (shouldInterpolateBetweenPings(prevPing, currPing)) {
        addInterpolatedPings(prevPing, currPing)
      } else {
        Seq(currPing)
      }
    }

    private def shouldInterpolateBetweenPings(prevPing: Ping, currPing: Ping)(
        implicit config: Config): Boolean = {
      currPing.mmsi.equals(prevPing.mmsi) &&
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
        prevPing: Ping,
        currPing: Ping,
        distanceThreshold: Long): Boolean =
      Haversine(
        prevPing.longitude,
        prevPing.latitude,
        currPing.longitude,
        currPing.latitude
      ) <= distanceThreshold

    private def isTimeBetweenPingsWithinThreshold(
        prevPing: Ping,
        currPing: Ping,
        timeThreshold: Long): Boolean =
      currPing.acquisitionTime.getTime - prevPing.acquisitionTime.getTime <= timeThreshold

    private def addInterpolatedPings(prevPing: Ping,
                                     currPing: Ping): Seq[Ping] = {

      var nextTime: Long = snapTo3Min(prevPing.acquisitionTime.getTime)
      val interpolatedPings = mutable.MutableList[Ping]()

      while (nextTime < currPing.acquisitionTime.getTime) {
        interpolatedPings += interpolateForNextTime(nextTime,
                                                    prevPing,
                                                    currPing)
        nextTime += TIME_STEP
      }

      if (nextTime == currPing.acquisitionTime.getTime) {
        interpolatedPings += currPing
      }

      interpolatedPings
    }

    private def snapTo3Min(epochTime: Long): Long = {
      val snappedTime: Long = (epochTime / TIME_STEP) * TIME_STEP
      if (snappedTime <= epochTime)
        snappedTime + TIME_STEP
      else
        snappedTime
    }

    private def interpolateForNextTime(nextTimeMillis: Long,
                                       prevPing: Ping,
                                       currPing: Ping): Ping = {
      val timeProportion
        : Double = (nextTimeMillis - prevPing.acquisitionTime.getTime).toDouble /
        (currPing.acquisitionTime.getTime - prevPing.acquisitionTime.getTime).toDouble

      val latGap: Double = currPing.latitude - prevPing.latitude
      val lonGap: Double = currPing.longitude - prevPing.longitude

      val newLat: Double = timeProportion * latGap + prevPing.latitude
      val newLon: Double = timeProportion * lonGap + prevPing.longitude

      Ping(prevPing.id,
           prevPing.mmsi,
           Timestamp.from(Instant.ofEpochMilli(nextTimeMillis)),
           newLon,
           newLat,
           prevPing.row)
    }

    private def prev(columnName: String): String = s"prev_$columnName"

  }

  implicit class ResampleAisPings(df: DataFrame) {

    def resample(implicit config: Config): DataFrame = {

      import df.sparkSession.implicits._
      import org.apache.spark.sql.functions._
      import ResampleAisPings.{prev, interpolateBetweenPingsFromTime}

      val windowSpec = Window
        .partitionBy(MMSI)
        .orderBy(ACQUISITION_TIME)

      df.withColumn(prev(ARKEVISTA_POS_ID),
                    lag(ARKEVISTA_POS_ID, 1).over(windowSpec))
        .withColumn(prev(MMSI), lag(MMSI, 1).over(windowSpec))
        .withColumn(prev(ACQUISITION_TIME),
                    lag(ACQUISITION_TIME, 1).over(windowSpec))
        .withColumn(prev(LONGITUDE), lag(LONGITUDE, 1).over(windowSpec))
        .withColumn(prev(LATITUDE), lag(LATITUDE, 1).over(windowSpec))
        .flatMap(row =>
          interpolateBetweenPingsFromTime(
            Ping(
              row.getAs[String](prev(ARKEVISTA_POS_ID)),
              row.getAs[String](prev(MMSI)),
              row.getAs[Timestamp](prev(ACQUISITION_TIME)),
              row.getAs[Double](prev(LONGITUDE)),
              row.getAs[Double](prev(LATITUDE)),
              row
            ),
            Ping(
              row.getAs[String](ARKEVISTA_POS_ID),
              row.getAs[String](MMSI),
              row.getAs[Timestamp](ACQUISITION_TIME),
              row.getAs[Double](LONGITUDE),
              row.getAs[Double](LATITUDE),
              row
            )
          ).map(ping => ping.asTuple()))
        .toDF(PARTITIONED_AIS_SCHEMA.fieldNames: _*)
    }
  }

}
