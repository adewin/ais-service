package uk.gov.ukho.ais.resampler

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ConfigParserTest {

  @Test
  def whenConfigIsValidThenConstructsConfigFromArguments(): Unit = {
    val args: Array[String] = Array(
      "-o",
      "out",
      "-p",
      "heatmap",
      "-l",
      "-r",
      "1",
      "-t",
      "12",
      "-d",
      "13",
      "-y",
      "2019",
      "-m",
      "1",
      "input-ais.csv.bz2"
    )

    val expectedConfig =
      Config.default.copy(
        outputDirectory = "out",
        outputFilePrefix = "heatmap",
        inputFiles = Seq("input-ais.csv.bz2"),
        isLocal = true,
        interpolationTimeThresholdMilliseconds = 12L,
        interpolationDistanceThresholdMeters = 13L,
        resolution = 1D
      )

    assertThat(ConfigParser.parse(args))
      .isEqualToComparingFieldByField(expectedConfig)
  }

  @Test(expected = classOf[IllegalStateException])
  def whenConfigContainsNegativeTimeInterpolationThenIllegalStateRaised()
    : Unit = {
    val args: Array[String] = Array(
      "-o",
      "out",
      "-l",
      "-r",
      "1",
      "-t",
      "-12",
      "-d",
      "13",
      "-y",
      "2019",
      "-m",
      "1"
    )

    ConfigParser.parse(args)
  }

  @Test(expected = classOf[IllegalStateException])
  def whenConfigContainsNegativeDistanceInterpolationThenIllegalStateRaised()
    : Unit = {
    val args: Array[String] = Array(
      "-o",
      "out",
      "-l",
      "-r",
      "1",
      "-t",
      "12",
      "-d",
      "-13",
      "-y",
      "2019",
      "-m",
      "1"
    )

    ConfigParser.parse(args)
  }

  @Test(expected = classOf[IllegalStateException])
  def whenConfigContainsNegativeResolutionThenIllegalStateRaised(): Unit = {
    val args: Array[String] = Array(
      "-o",
      "out",
      "-l",
      "-r",
      "-1",
      "-t",
      "12",
      "-d",
      "13",
      "-y",
      "2019",
      "-m",
      "1"
    )

    ConfigParser.parse(args)
  }

  @Test(expected = classOf[IllegalStateException])
  def whenConfigContainsYearBefore1970ThenIllegalStateRaised(): Unit = {
    val args: Array[String] = Array(
      "-o",
      "out",
      "-l",
      "-r",
      "1",
      "-t",
      "12",
      "-d",
      "13",
      "-y",
      "1969",
      "-m",
      "1"
    )

    ConfigParser.parse(args)
  }

  @Test(expected = classOf[IllegalStateException])
  def whenConfigContainsMonthLessThan1ThenIllegalStateRaised(): Unit = {
    val args: Array[String] = Array(
      "-o",
      "out",
      "-l",
      "-r",
      "1",
      "-t",
      "12",
      "-d",
      "13",
      "-y",
      "2019",
      "-m",
      "0"
    )

    ConfigParser.parse(args)
  }

  @Test(expected = classOf[IllegalStateException])
  def whenConfigContainsMonthMoreThan12ThenIllegalStateRaised(): Unit = {
    val args: Array[String] = Array(
      "-o",
      "out",
      "-l",
      "-r",
      "1",
      "-t",
      "12",
      "-d",
      "13",
      "-y",
      "2019",
      "-m",
      "13"
    )

    ConfigParser.parse(args)
  }

  @Test(expected = classOf[IllegalStateException])
  def whenConfigContainsMissingRequiredArgsThenIllegalStateRaised(): Unit = {
    ConfigParser.parse(Array())
  }
}
