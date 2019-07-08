package uk.gov.ukho.ais.rasters

import java.sql.Timestamp
import java.time.format.DateTimeFormatter
import java.time.{Instant, LocalDate, LocalDateTime, ZoneId}

import org.apache.spark.rdd.RDD
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import uk.gov.ukho.ais.rasters.AisDataFilters.RDDFilters

import scala.collection.JavaConverters._

class AisDataFiltersTest {

  Session.init(true)

  private final val SINGLE_PING_INPUT_PATH: String = "ais_single_ping.txt"
  private final val OUTPUT_DIR: String = "out"
  private final val DRAUGHT_FILE: String = "draught_data.txt"
  private final val STATIC_DATA_FILE: String = "test_static_data.txt"
  private final val PREFIX: String = "prefix"
  private final val RESOLUTION: String = "1"
  private final val TIME_THRESHOLD: String = "12"
  private final val DISTANCE_THRESHOLD: String = "13"

  private var testConfig: Config = _

  @Test
  def whenFilteringOnMsgTypeThenInvalidTypesFilteredOut(): Unit = {
    val timestamp = Timestamp.from(Instant.now())

    val expectedRows: Seq[(String, ShipPing)] = Seq(
      ("mmsi1", ShipPing("mmsi1", timestamp.getTime, 0d, 0d, 0d, 1)),
      ("mmsi1", ShipPing("mmsi1", timestamp.getTime, 0d, 0d, 0d, 2)),
      ("mmsi1", ShipPing("mmsi1", timestamp.getTime, 0d, 0d, 0d, 3)),
      ("mmsi1", ShipPing("mmsi1", timestamp.getTime, 0d, 0d, 0d, 18)),
      ("mmsi1", ShipPing("mmsi1", timestamp.getTime, 0d, 0d, 0d, 19))
    )

    val rdd: RDD[(String, ShipPing)] =
      Session.sparkSession.sparkContext.parallelize(
        Seq(
          ("mmsi1", ShipPing("mmsi1", timestamp.getTime, 0d, 0d, 0d, 0)),
          ("mmsi1", ShipPing("mmsi1", timestamp.getTime, 0d, 0d, 0d, 1)),
          ("mmsi1", ShipPing("mmsi1", timestamp.getTime, 0d, 0d, 0d, 2)),
          ("mmsi1", ShipPing("mmsi1", timestamp.getTime, 0d, 0d, 0d, 3)),
          ("mmsi1", ShipPing("mmsi1", timestamp.getTime, 0d, 0d, 0d, 4)),
          ("mmsi1", ShipPing("mmsi1", timestamp.getTime, 0d, 0d, 0d, 17)),
          ("mmsi1", ShipPing("mmsi1", timestamp.getTime, 0d, 0d, 0d, 18)),
          ("mmsi1", ShipPing("mmsi1", timestamp.getTime, 0d, 0d, 0d, 19)),
          ("mmsi1", ShipPing("mmsi1", timestamp.getTime, 0d, 0d, 0d, 20))
        ))

    val result: RDD[(String, ShipPing)] = rdd.filterByValidMessageType()

    assertThat(result.collect())
      .containsExactlyInAnyOrderElementsOf(expectedRows.asJava)
  }

  @Test
  def whenFilteringOnDatesThenDatesOutsideRangesAreFilteredOut(): Unit = {
    val testTime = Instant.now()

    val expectedRows: Seq[(String, ShipPing)] = Seq(
      ("mmsi1",
       ShipPing("mmsi1",
                Timestamp.from(testTime.plusSeconds(60)).getTime,
                0d,
                0d,
                0d,
                0)),
      ("mmsi1",
       ShipPing("mmsi1",
                Timestamp.from(testTime.plusSeconds(120)).getTime,
                0d,
                0d,
                0d,
                0)),
      ("mmsi1",
       ShipPing("mmsi1",
                Timestamp.from(testTime.plusSeconds(180)).getTime,
                0d,
                0d,
                0d,
                0))
    )

    val rdd: RDD[(String, ShipPing)] =
      Session.sparkSession.sparkContext.parallelize(
        Seq(
          ("mmsi1",
           ShipPing("mmsi1",
                    Timestamp.from(testTime.plusSeconds(60)).getTime,
                    0d,
                    0d,
                    0d,
                    0)),
          ("mmsi1",
           ShipPing("mmsi1",
                    Timestamp.from(testTime.plusSeconds(120)).getTime,
                    0d,
                    0d,
                    0d,
                    0)),
          ("mmsi1",
           ShipPing("mmsi1",
                    Timestamp.from(testTime.plusSeconds(180)).getTime,
                    0d,
                    0d,
                    0d,
                    0)),
          ("mmsi1",
           ShipPing("mmsi1",
                    Timestamp.from(testTime.plusSeconds(300)).getTime,
                    0d,
                    0d,
                    0d,
                    0)),
          ("mmsi1",
           ShipPing("mmsi1",
                    Timestamp.from(testTime.plusSeconds(600)).getTime,
                    0d,
                    0d,
                    0d,
                    0)),
          ("mmsi1",
           ShipPing("mmsi1",
                    Timestamp.from(testTime.plusSeconds(720)).getTime,
                    0d,
                    0d,
                    0d,
                    0))
        ))

    val result: RDD[(String, ShipPing)] = rdd.filterPingsByTimePeriod(
      Timestamp.from(testTime),
      Timestamp.from(testTime.plusSeconds(300)))

    assertThat(result.collect())
      .containsExactlyInAnyOrderElementsOf(expectedRows.asJava)
  }

  @Test
  def whenFilteringOnVesselDraughtThenVesselsOutsideOfRangeFilteredOut()
    : Unit = {

    val draughtFilterPoints =
      Array[VesselDraughtRange](VesselDraughtRange(0, 1),
                                VesselDraughtRange(1, 2.5),
                                VesselDraughtRange(2.5, 2000))

    val testTime = Instant.now()

    val expectedRows: Seq[(String, ShipPing)] = Seq(
      ("mmsi1", ShipPing("mmsi1", testTime.toEpochMilli, 0D, 0D, 1D, 1)),
      ("mmsi1", ShipPing("mmsi1", testTime.toEpochMilli, 0D, 0D, 2D, 1)),
      ("mmsi1", ShipPing("mmsi1", testTime.toEpochMilli, 0D, 0D, 2.45D, 1))
    )

    val rdd: RDD[(String, ShipPing)] =
      Session.sparkSession.sparkContext.parallelize(
        Seq(
          ("mmsi1", ShipPing("mmsi1", testTime.toEpochMilli, 0D, 0D, 1D, 1)),
          ("mmsi1", ShipPing("mmsi1", testTime.toEpochMilli, 0D, 0D, 2D, 1)),
          ("mmsi1", ShipPing("mmsi1", testTime.toEpochMilli, 0D, 0D, 2.45D, 1)),
          ("mmsi1", ShipPing("mmsi1", testTime.toEpochMilli, 0D, 0D, 10D, 1)),
          ("mmsi1", ShipPing("mmsi1", testTime.toEpochMilli, 0D, 0D, 100D, 1))
        ))

    val result: RDD[(String, ShipPing)] =
      rdd.filterPingsByDraught(Option(1), draughtFilterPoints)

    assertThat(result.collect())
      .containsExactlyInAnyOrderElementsOf(expectedRows.asJava)
  }

  @Test
  def whenUnknownDraughtFilteringThenVesselsWithDraughtAreFilteredOut()
    : Unit = {
    val draughtFilterPoints =
      Array[VesselDraughtRange](VesselDraughtRange(0, 1),
                                VesselDraughtRange(1, 2.5),
                                VesselDraughtRange(2.5, 2000))

    val testTime = Instant.now()

    val expectedRows: Seq[(String, ShipPing)] = Seq(
      ("mmsi1", ShipPing("mmsi1", testTime.toEpochMilli, 0D, 0D, -1, 1)),
      ("mmsi1", ShipPing("mmsi1", testTime.toEpochMilli, 0D, 0D, 0D, 1))
    )

    val rdd: RDD[(String, ShipPing)] =
      Session.sparkSession.sparkContext.parallelize(
        Seq(
          ("mmsi1", ShipPing("mmsi1", testTime.toEpochMilli, 0D, 0D, 1D, 1)),
          ("mmsi1", ShipPing("mmsi1", testTime.toEpochMilli, 0D, 0D, 2D, 1)),
          ("mmsi1", ShipPing("mmsi1", testTime.toEpochMilli, 0D, 0D, -1, 1)),
          ("mmsi1", ShipPing("mmsi1", testTime.toEpochMilli, 0D, 0D, 0D, 1)),
          ("mmsi1", ShipPing("mmsi1", testTime.toEpochMilli, 0D, 0D, 1D, 1)),
          ("mmsi1", ShipPing("mmsi1", testTime.toEpochMilli, 0D, 0D, 10, 1)),
          ("mmsi1", ShipPing("mmsi1", testTime.toEpochMilli, 0D, 0D, 100D, 1))
        ))

    val result: RDD[(String, ShipPing)] =
      rdd.filterPingsByDraught(Option(-1), draughtFilterPoints)

    assertThat(result.collect())
      .containsExactlyInAnyOrderElementsOf(expectedRows.asJava)
  }

  @Test
  def whenFilteringShipPingsOnVesselDraughtAndConfigDoesNotSpecifyDraughtIndexThenNoPingsFilteredOut()
    : Unit = {
    val draughtFilterPoints =
      Array[VesselDraughtRange](VesselDraughtRange(0, 1),
                                VesselDraughtRange(1, 2.5),
                                VesselDraughtRange(2.5, 2000))

    val testTime = Instant.now()

    val expectedRows: Seq[(String, ShipPing)] = Seq(
      ("mmsi1", ShipPing("mmsi1", testTime.toEpochMilli, 0D, 0D, 1D, 1)),
      ("mmsi1", ShipPing("mmsi1", testTime.toEpochMilli, 0D, 0D, 2D, 1)),
      ("mmsi1", ShipPing("mmsi1", testTime.toEpochMilli, 0D, 0D, -1, 1)),
      ("mmsi1", ShipPing("mmsi1", testTime.toEpochMilli, 0D, 0D, 0D, 1)),
      ("mmsi1", ShipPing("mmsi1", testTime.toEpochMilli, 0D, 0D, 1D, 1)),
      ("mmsi1", ShipPing("mmsi1", testTime.toEpochMilli, 0D, 0D, 10, 1)),
      ("mmsi1", ShipPing("mmsi1", testTime.toEpochMilli, 0D, 0D, 100D, 1))
    )

    val rdd: RDD[(String, ShipPing)] =
      Session.sparkSession.sparkContext.parallelize(expectedRows)

    val result: RDD[(String, ShipPing)] =
      rdd.filterPingsByDraught(None, draughtFilterPoints)

    assertThat(result.collect())
      .containsExactlyInAnyOrderElementsOf(expectedRows.asJava)
  }

  @Test
  def whenFilteringShipPingsThenAllFiltersApplied(): Unit = {
    val testTime = Instant.now()
    testConfig = setConfigToUseDate(
      LocalDateTime.ofInstant(testTime, ZoneId.systemDefault()).toLocalDate)

    val shipPingInTimeAndVesselDraughtRanges = createShipPingWith(
      "mmsi1",
      Timestamp.from(testTime.plusSeconds(60)).getTime,
      1D,
      1)

    val shipPingInTimeRangeAndLowerBoundVesselDraughtRange = createShipPingWith(
      "mmsi1",
      Timestamp.from(testTime.plusSeconds(60)).getTime,
      1D,
      1)

    val shipPingInTimeRangeAndVesselDraughtRangeButWrongMessageType =
      createShipPingWith("mmsi1",
                         Timestamp.from(testTime.plusSeconds(60)).getTime,
                         0d,
                         5)

    val shipPingInTimeRangeButOutsideVesselDraughtRange = createShipPingWith(
      "mmsi1",
      Timestamp.from(testTime.plusSeconds(60)).getTime,
      4d,
      1)

    val shipPingInTimeRangeButUnknownVesselDraughtRange = createShipPingWith(
      "mmsi2",
      Timestamp.from(testTime.plusSeconds(60)).getTime,
      -1d,
      1)

    val shipPingOutsideOfTimeRange = createShipPingWith(
      "mmsi1",
      Timestamp.from(testTime.plusSeconds(48 * 60 * 60)).getTime,
      0d,
      1)

    val shipPingWithoutAnyMatchingAttributes = createShipPingWith(
      "mmsi3",
      Timestamp.from(testTime.plusSeconds(48 * 60 * 60)).getTime,
      19d,
      10)

    val expectedRows: Seq[(String, ShipPing)] = Seq(
      shipPingInTimeAndVesselDraughtRanges,
      shipPingInTimeRangeAndLowerBoundVesselDraughtRange
    )

    val rdd: RDD[(String, ShipPing)] =
      Session.sparkSession.sparkContext.parallelize(
        Seq(
          shipPingInTimeAndVesselDraughtRanges,
          shipPingInTimeRangeAndLowerBoundVesselDraughtRange,
          shipPingInTimeRangeAndVesselDraughtRangeButWrongMessageType,
          shipPingInTimeRangeButOutsideVesselDraughtRange,
          shipPingOutsideOfTimeRange,
          shipPingInTimeRangeButUnknownVesselDraughtRange,
          shipPingWithoutAnyMatchingAttributes
        ))

    val result: RDD[(String, ShipPing)] = rdd.filterShipPings(testConfig)

    assertThat(result.collect())
      .containsExactlyInAnyOrderElementsOf(expectedRows.asJava)
  }

  private def setConfigToUseDate(testDate: LocalDate): Config = {
    ConfigParser.parse(
      Array(
        "-i",
        ResourceService.copyFileToFileSystem(SINGLE_PING_INPUT_PATH),
        "-o",
        OUTPUT_DIR,
        "-p",
        PREFIX,
        "-r",
        RESOLUTION,
        "-t",
        TIME_THRESHOLD,
        "-d",
        DISTANCE_THRESHOLD,
        "-s",
        testDate.format(DateTimeFormatter.ISO_DATE),
        "-e",
        testDate.plusDays(1).format(DateTimeFormatter.ISO_DATE),
        "--draughtConfigFile",
        ResourceService.copyFileToFileSystem(DRAUGHT_FILE),
        "--staticDataFile",
        ResourceService.copyFileToFileSystem(STATIC_DATA_FILE),
        "--draughtIndex",
        "0"
      ))
  }

  private def createShipPingWith(mmsi: String,
                                 timestamp: Long,
                                 vesselDraught: Double,
                                 messageType: Int): (String, ShipPing) = {
    (mmsi, ShipPing(mmsi, timestamp, 0D, 0D, vesselDraught, messageType))
  }
}
