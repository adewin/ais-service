package uk.gov.ukho.ais.resample

import java.sql.Timestamp
import java.time.{Instant, LocalDateTime, ZoneOffset}

import geotrellis.util.Haversine
import org.apache.spark.sql.catalyst.encoders.RowEncoder
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import org.apache.spark.sql.{DataFrame, Row}
import uk.gov.ukho.ais.Schema

import scala.collection.mutable

object ResamplingFilter {

  private final val TIME_STEP: Long = 3 * 60 * 1000

  implicit class ResampleAisPings(df: DataFrame) {

    import df.sqlContext.implicits._

    def resample(implicit config: Config): DataFrame = {
      df.groupByKey(row => row.getAs[String](Schema.MMSI))
        .flatMapGroups {
          case (_: String, rows: Iterator[Row]) =>
            ResamplingFilter.resamplePings(rows.toSeq)
        } {
          RowEncoder(Schema.PARTITIONED_AIS_SCHEMA)
        }
    }
  }

  def resamplePings(rows: Seq[Row])(implicit config: Config): Seq[Row] = {

    val outputPings: mutable.MutableList[Row] = mutable.MutableList()
    var prevPing: Row = null

    rows
      .sortBy(getAcquisitionTimeAsEpochMillis)
      .foreach(currPing => {

        if (prevPing == null) {
          outputPings += currPing
        } else {
          outputPings ++= interpolateBetweenPingsFromTime(
            prevPing,
            currPing,
            getAcquisitionTimeAsEpochMillis(outputPings.last))
        }

        prevPing = currPing
      })

    outputPings
  }

  private def getAcquisitionTimeAsEpochMillis(row: Row): Long =
    row.getAs[Timestamp](Schema.ACQUISITION_TIME).getTime

  private def interpolateBetweenPingsFromTime(
      prevPing: Row,
      currPing: Row,
      fromTime: Long)(implicit config: Config): Seq[Row] = {
    if (shouldInterpolateBetweenPings(prevPing, currPing)) {
      addInterpolatedPings(prevPing, currPing, fromTime)
    } else {
      List(currPing)
    }
  }

  private def shouldInterpolateBetweenPings(prevPing: Row, currPing: Row)(
      implicit config: Config): Boolean = {
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
      prevPing: Row,
      currPing: Row,
      distanceThreshold: Long): Boolean =
    Haversine(
      prevPing.getAs[Double](Schema.LONGITUDE),
      prevPing.getAs[Double](Schema.LATITUDE),
      currPing.getAs[Double](Schema.LONGITUDE),
      currPing.getAs[Double](Schema.LATITUDE)
    ) <= distanceThreshold

  private def isTimeBetweenPingsWithinThreshold(prevPing: Row,
                                                currPing: Row,
                                                timeThreshold: Long): Boolean =
    getAcquisitionTimeAsEpochMillis(currPing) - getAcquisitionTimeAsEpochMillis(
      prevPing) <= timeThreshold

  private def addInterpolatedPings(prevPing: Row,
                                   currPing: Row,
                                   fromTime: Long): List[Row] = {
    var nextTime: Long = fromTime + TIME_STEP
    val interpolatedPings = mutable.MutableList[Row]()

    while (nextTime < getAcquisitionTimeAsEpochMillis(currPing)) {
      interpolatedPings += interpolateForNextTime(nextTime, prevPing, currPing)
      nextTime += TIME_STEP
    }

    if (nextTime == getAcquisitionTimeAsEpochMillis(currPing)) {
      interpolatedPings += currPing
    }

    interpolatedPings.toList
  }

  private def interpolateForNextTime(nextTimeMillis: Long,
                                     prevPing: Row,
                                     currPing: Row): Row = {

    val timeProportion: Double = (nextTimeMillis - getAcquisitionTimeAsEpochMillis(
      prevPing)).toDouble /
      (getAcquisitionTimeAsEpochMillis(currPing) - getAcquisitionTimeAsEpochMillis(
        prevPing)).toDouble

    val latGap: Double = currPing.getAs[Double](Schema.LATITUDE) - prevPing
      .getAs[Double](Schema.LATITUDE)
    val lonGap: Double = currPing.getAs[Double](Schema.LONGITUDE) - prevPing
      .getAs[Double](Schema.LONGITUDE)

    val newLat: Double = timeProportion * latGap + prevPing.getAs[Double](
      Schema.LATITUDE)
    val newLon: Double = timeProportion * lonGap + prevPing.getAs[Double](
      Schema.LONGITUDE)

    new GenericRowWithSchema(
      Schema.PARTITIONED_AIS_SCHEMA.fieldNames.map {
        case Schema.ACQUISITION_TIME =>
          Timestamp.from(Instant.ofEpochMilli(nextTimeMillis))
        case Schema.LATITUDE  => newLat
        case Schema.LONGITUDE => newLon
        case field            => prevPing.getAs(field)
      },
      Schema.PARTITIONED_AIS_SCHEMA
    )
  }
}
