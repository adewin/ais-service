package uk.gov.ukho.ais.rasters

import java.sql.Timestamp

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ConfigParserTest {

  @Test
  def whenConfigIsValidThenConstructsConfigFromArguments(): Unit = {
    val args: Array[String] = TestingConfigBuilder
      .fromDefaults()
      .build()

    val expectedConfig =
      Config(
        inputPath = "in",
        outputDirectory = "out",
        draughtConfigFile = "draught",
        staticDataFile = "static",
        outputFilenamePrefix = "prefix",
        isLocal = true,
        resolution = 1D,
        interpolationTimeThresholdMilliseconds = 12L,
        interpolationDistanceThresholdMeters = 13L,
        startPeriod = Timestamp.valueOf("2019-01-01 00:00:00"),
        endPeriod = Timestamp.valueOf("2020-01-01 00:00:00"),
        draughtIndex = None
      )

    ConfigParser.parse(args)

    assertThat(ConfigParser.config)
      .isEqualToComparingFieldByField(expectedConfig)
  }

  @Test
  def whenConfigContainsUnknownDraughtIndexThenDraughtIndexIsMinusOne()
    : Unit = {
    val args: Array[String] = TestingConfigBuilder
      .fromDefaults()
      .draughtIndex("unknown")
      .build()

    ConfigParser.parse(args)

    assertThat(ConfigParser.config.draughtIndex.get).isEqualTo(-1)
  }

  @Test
  def whenConfigDoesNotContainDraughtIndexThenDraughtIndexIsNone(): Unit = {
    val args: Array[String] = TestingConfigBuilder
      .fromDefaults()
      .build()

    ConfigParser.parse(args)

    assertThat(ConfigParser.config.draughtIndex).isEqualTo(None)
  }

  @Test(expected = classOf[IllegalStateException])
  def whenConfigContainsDraughtIndexIsNotANumberThenNoConfigReturned(): Unit = {
    val args: Array[String] = TestingConfigBuilder
      .fromDefaults()
      .draughtIndex("number")
      .build()

    ConfigParser.parse(args)
    ConfigParser.config
  }

  @Test(expected = classOf[IllegalStateException])
  def whenConfigContainsNegativeDraughtIndexThenNoConfigReturned(): Unit = {
    val args: Array[String] = TestingConfigBuilder
      .fromDefaults()
      .draughtIndex("-1")
      .build()

    ConfigParser.parse(args)
    ConfigParser.config
  }

  @Test(expected = classOf[IllegalStateException])
  def whenConfigContainsNegativeValueThenNoConfigReturned(): Unit = {
    val args: Array[String] = TestingConfigBuilder
      .fromDefaults()
      .interpolationTimeThresholdMilliseconds("-1000")
      .build()

    ConfigParser.parse(args)
    ConfigParser.config
  }

  @Test
  def whenConfigContainsDraughtIndexValueThenCorrectConfigReturned(): Unit = {
    val args: Array[String] = TestingConfigBuilder
      .fromDefaults()
      .draughtIndex("12")
      .build()

    ConfigParser.parse(args)

    assertThat(ConfigParser.config.draughtIndex.get).isEqualTo(12)
  }

  @Test(expected = classOf[IllegalStateException])
  def whenStartPeriodNotValidDateThenNoConfigReturned(): Unit = {
    val args: Array[String] = TestingConfigBuilder
      .fromDefaults()
      .startPeriod("NOT A DATE")
      .build()

    ConfigParser.parse(args)
    ConfigParser.config
  }

  @Test(expected = classOf[IllegalStateException])
  def whenEndPeriodDateTimeThenNoConfigReturned(): Unit = {
    val args: Array[String] = TestingConfigBuilder
      .fromDefaults()
      .endPeriod("2099-01-01 00:00:00")
      .build()

    ConfigParser.parse(args)
    ConfigParser.config
  }
}
