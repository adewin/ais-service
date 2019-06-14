package uk.gov.ukho.ais.rasters

import java.sql.Timestamp

import org.assertj.core.api.Assertions.{assertThat, fail}
import org.junit.Test

@SuppressWarnings(Array("org.wartremover.warts.Null"))
class ConfigTest {
  private final val INPUT_PATH: String = "in"
  private final val OUTPUT_DIR: String = "out"
  private final val PREFIX: String = "prefix"
  private final val RESOLUTION: String = "1"
  private final val START_PERIOD = "2019-01-01"
  private final val END_PERIOD = "2019-12-31"
  private final val TIME_THRESHOLD: String = "12"
  private final val DISTANCE_THRESHOLD: String = "13"

  @Test
  def whenConfigIsValidThenConstructsConfigFromArguments(): Unit = {
    val args: Array[String] = Array(
      "-i",
      INPUT_PATH,
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
      START_PERIOD,
      "-e",
      END_PERIOD
    )

    val expectedConfig =
      new Config(
        INPUT_PATH,
        OUTPUT_DIR,
        PREFIX,
        false,
        RESOLUTION.toDouble,
        TIME_THRESHOLD.toLong,
        DISTANCE_THRESHOLD.toLong,
        Timestamp.valueOf(s"$START_PERIOD 00:00:00"),
        Timestamp.valueOf("2020-01-01 00:00:00")
      )

    Config.parse(args) match {
      case Some(config) =>
        assertThat(config)
          .isEqualToComparingFieldByField(expectedConfig)
      case _ => fail("Could not parse arguments")
    }
  }

  @Test
  def whenConfigContainsNegativeValueThenNoConfigReturned(): Unit = {
    val args: Array[String] = Array(
      "-i",
      INPUT_PATH,
      "-o",
      OUTPUT_DIR,
      "-p",
      PREFIX,
      "-r",
      RESOLUTION,
      "-t",
      "-" + TIME_THRESHOLD,
      "-d",
      DISTANCE_THRESHOLD,
      "-s",
      START_PERIOD,
      "-e",
      END_PERIOD
    )

    Config.parse(args) match {
      case Some(config) => fail("Expected no config back")
      case _            =>
    }
  }

  @Test
  def whenStartPeriodNotValidDateThenNoConfigReturned(): Unit = {
    val args: Array[String] = Array(
      "-i",
      INPUT_PATH,
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
      "NOT A DATE",
      "-e",
      END_PERIOD
    )

    Config.parse(args) match {
      case Some(_) =>
        fail("Should not have parsed arguments")
      case _ =>
    }
  }

  @Test
  def whenEndPeriodDateTimeThenNoConfigReturned(): Unit = {
    val args: Array[String] = Array(
      "-i",
      INPUT_PATH,
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
      START_PERIOD,
      "-e",
      s"$END_PERIOD 00:00:00"
    )

    Config.parse(args) match {
      case Some(_) =>
        fail("Should not have parsed arguments")
      case _ =>
    }
  }
}
