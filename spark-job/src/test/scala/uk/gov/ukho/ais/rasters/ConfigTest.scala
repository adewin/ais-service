package uk.gov.ukho.ais.rasters

import org.assertj.core.api.Assertions.{assertThat, fail}
import org.junit.Test

@SuppressWarnings(Array("org.wartremover.warts.Null"))
class ConfigTest {
  private final val INPUT_PATH: String = "in"
  private final val OUTPUT_DIR: String = "out"
  private final val PREFIX: String = "prefix"
  private final val RESOLUTION: String = "1"
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
      DISTANCE_THRESHOLD
    )

    val expectedConfig =
      new Config(INPUT_PATH,
                 OUTPUT_DIR,
                 PREFIX,
                 false,
                 RESOLUTION.toDouble,
                 TIME_THRESHOLD.toLong,
                 DISTANCE_THRESHOLD.toLong)

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
      DISTANCE_THRESHOLD
    )

    Config.parse(args) match {
      case Some(config) => fail("Expected no config back")
      case _            =>
    }
  }
}
