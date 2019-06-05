package uk.gov.ukho.ais.rasters

import geotrellis.vector.Polygon
import geotrellis.vector.io.wkt.WKT
import org.junit.Test
import org.assertj.core.api.Assertions.{fail, assertThat}

@SuppressWarnings(Array("org.wartremover.warts.Null"))
class ConfigTest {
  private final val INPUT_PATH: String = "in"
  private final val OUTPUT_DIR: String = "out"
  private final val PREFIX: String = "prefix"
  private final val WORLD_PREFIX: String = "world"
  private final val RESOLUTION: String = "1"

  @Test
  def whenConfigIsValidThenConstructsConfigFromArguments(): Unit = {
    val args: Array[String] =
      Array("-i", INPUT_PATH, "-o", OUTPUT_DIR, "-p", PREFIX, "-r", RESOLUTION)

    val expectedConfig =
      new Config(INPUT_PATH, OUTPUT_DIR, PREFIX, false, RESOLUTION.toDouble)

    Config.parse(args) match {
      case Some(config) =>
        assertThat(config)
          .isEqualToComparingFieldByField(expectedConfig)
      case _ => fail("Could not parse arguments")
    }
  }

  @Test
  def whenNoPrefixSuppliedThenPrefixDefaultsToWorld(): Unit = {
    val args: Array[String] =
      Array("-i", INPUT_PATH, "-o", OUTPUT_DIR, "-r", RESOLUTION)

    Config.parse(args) match {
      case Some(config) =>
        assertThat(config.outputFilenamePrefix).isEqualTo(WORLD_PREFIX)
      case _ => fail("Could not parse arguments")
    }
  }
}
