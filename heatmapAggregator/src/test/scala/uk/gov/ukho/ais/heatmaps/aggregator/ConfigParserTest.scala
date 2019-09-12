package uk.gov.ukho.ais.heatmaps.aggregator

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ConfigParserTest {

  @Test(expected = classOf[IllegalStateException])
  def whenConfigIsEmptyThenIllegalStateExceptionRaised(): Unit = {
    ConfigParser.parse(Array())
  }

  @Test
  def whenConfigIsValidThenConstructsConfigFromArguments(): Unit = {
    val args: Array[String] = Array(
      "-b",
      "fredbucket"
    )

    val expectedConfig: Config =
      Config.default.copy(heatmapsDirectory = "fredbucket")

    assertThat(ConfigParser.parse(args))
      .isEqualToComparingFieldByField(expectedConfig)
  }

  @Test(expected = classOf[IllegalStateException])
  def whenConfigIsInvalidThenIllegalStateExceptionRaised(): Unit = {
    val args: Array[String] = Array(
      "-f",
      "wibble"
    )

    ConfigParser.parse(args)
  }
}
