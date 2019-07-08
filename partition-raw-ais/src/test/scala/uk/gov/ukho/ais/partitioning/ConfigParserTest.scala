package uk.gov.ukho.ais.partitioning

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ConfigParserTest {

  @Test
  def whenAllShortParamsPresentThenCorrectlyReadsConfig(): Unit = {
    val config = ConfigParser.parse(Array("-i", "inputFile", "-o", "outputDir"))

    assertThat(config.inputPath).isEqualTo("inputFile")
    assertThat(config.outputDirectory).isEqualTo("outputDir")
  }

  @Test
  def whenAllLongParamsPresentThenCorrectlyReadsConfig(): Unit = {
    val config =
      ConfigParser.parse(Array("--input", "inputFile", "--output", "outputDir"))

    assertThat(config.inputPath).isEqualTo("inputFile")
    assertThat(config.outputDirectory).isEqualTo("outputDir")
  }

  @Test(expected = classOf[IllegalStateException])
  def whenMissingInputThenIllegalStateExceptionThrown(): Unit = {
    ConfigParser.parse(Array("--output", "outputDir"))
  }

  @Test(expected = classOf[IllegalStateException])
  def whenMissingOutputThenIllegalStateExceptionThrown(): Unit = {
    ConfigParser.parse(Array("--input", "inputFile"))
  }

  @Test(expected = classOf[IllegalStateException])
  def whenMissingArgsThenIllegalStateExceptionThrown(): Unit = {
    ConfigParser.parse(Array())
  }
}
