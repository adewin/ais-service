package uk.gov.ukho.ais.rasters

import java.sql.Timestamp
import java.time.Instant

import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{Row, SparkSession}
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import uk.gov.ukho.ais.rasters.Filters.RDDFilters

import scala.collection.JavaConverters._

class FiltersTest {

  val spark: SparkSession = {
    SparkSession
      .builder()
      .master("local")
      .appName("Spark Testing")
      .getOrCreate()
  }

  @Test
  def whenFilteringOnMsgTypeThenInvalidTypesFilteredOut(): Unit = {
    val timestamp = Timestamp.from(Instant.now())

    val expectedRows: Seq[Row] = Seq(
      Row("mmsi1", timestamp, 1d, 1d, 1),
      Row("mmsi1", timestamp, 2d, 2d, 2),
      Row("mmsi1", timestamp, 3d, 3d, 3),
      Row("mmsi1", timestamp, 18d, 18d, 18),
      Row("mmsi1", timestamp, 19d, 19d, 19)
    )

    val rdd: RDD[Row] = spark.sparkContext.parallelize(
      Seq(
        Row("mmsi1", timestamp, 0d, 0d, 0),
        Row("mmsi1", timestamp, 1d, 1d, 1),
        Row("mmsi1", timestamp, 2d, 2d, 2),
        Row("mmsi1", timestamp, 3d, 3d, 3),
        Row("mmsi1", timestamp, 4d, 4d, 4),
        Row("mmsi1", timestamp, 17d, 17d, 17),
        Row("mmsi1", timestamp, 18d, 18d, 18),
        Row("mmsi1", timestamp, 19d, 19d, 19),
        Row("mmsi1", timestamp, 20d, 20d, 20)
      ))

    val result: RDD[Row] = rdd.filterByValidMessageType()

    assertThat(result.collect())
      .containsExactlyInAnyOrderElementsOf(expectedRows.asJava)
  }

  @Test
  def whenFilteringOnDatesThenDatesOutsideRangesAreFilteredOut(): Unit = {
    val testTime = Instant.now()

    val expectedRows: Seq[Row] = Seq(
      Row("mmsi1", Timestamp.from(testTime.plusSeconds(60)), 0d, 0d, 0),
      Row("mmsi2", Timestamp.from(testTime.plusSeconds(120)), 1d, 1d, 1),
      Row("mmsi3", Timestamp.from(testTime.plusSeconds(180)), 2d, 2d, 2)
    )

    val rdd: RDD[Row] = spark.sparkContext.parallelize(
      Seq(
        Row("mmsi1", Timestamp.from(testTime.plusSeconds(60)), 0d, 0d, 0),
        Row("mmsi2", Timestamp.from(testTime.plusSeconds(120)), 1d, 1d, 1),
        Row("mmsi3", Timestamp.from(testTime.plusSeconds(180)), 2d, 2d, 2),
        Row("mmsi4", Timestamp.from(testTime.plusSeconds(300)), 3d, 3d, 3),
        Row("mmsi5", Timestamp.from(testTime.plusSeconds(600)), 4d, 4d, 4),
        Row("mmsi6", Timestamp.from(testTime.plusSeconds(720)), 17d, 17d, 17)
      ))

    val result: RDD[Row] = rdd.filterPingsByTimePeriod(
      Timestamp.from(testTime),
      Timestamp.from(testTime.plusSeconds(300)))

    assertThat(result.collect())
      .containsExactlyInAnyOrderElementsOf(expectedRows.asJava)
  }
}
