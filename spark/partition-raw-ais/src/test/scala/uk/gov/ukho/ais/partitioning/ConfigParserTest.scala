package uk.gov.ukho.ais.partitioning

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ConfigParserTest {

  @Test
  def whenAllShortParamsPresentThenCorrectlyReadsConfig(): Unit = {
    val config = ConfigParser.parse(
      Array(
        "-i",
        "inputFile",
        "-o",
        "outputDir",
        "-d",
        "outputDatabase",
        "-t",
        "outputTable"
      ))

    assertThat(config.inputPath).isEqualTo("inputFile")
    assertThat(config.outputDirectory).isEqualTo("outputDir")
    assertThat(config.outputDatabase.get).isEqualTo("outputDatabase")
    assertThat(config.outputTable.get).isEqualTo("outputTable")
  }

  @Test
  def whenAllLongParamsPresentThenCorrectlyReadsConfig(): Unit = {
    val config =
      ConfigParser.parse(
        Array(
          "--input",
          "inputFile",
          "--output",
          "outputDir",
          "--database",
          "outputDatabase",
          "--table",
          "outputTable"
        ))

    assertThat(config.inputPath).isEqualTo("inputFile")
    assertThat(config.outputDirectory).isEqualTo("outputDir")
    assertThat(config.outputDatabase.get).isEqualTo("outputDatabase")
    assertThat(config.outputTable.get).isEqualTo("outputTable")
  }

  @Test(expected = classOf[IllegalStateException])
  def whenMissingInputThenIllegalStateExceptionThrown(): Unit = {
    ConfigParser.parse(
      Array(
        "-o",
        "outputDir",
        "-d",
        "outputDatabase",
        "-t",
        "outputTable"
      ))
  }

  @Test(expected = classOf[IllegalStateException])
  def whenMissingOutputThenIllegalStateExceptionThrown(): Unit = {
    ConfigParser.parse(
      Array(
        "-i",
        "inputFile",
        "-d",
        "outputDatabase",
        "-t",
        "outputTable"
      ))
  }

  @Test(expected = classOf[IllegalStateException])
  def whenMissingArgsThenIllegalStateExceptionThrown(): Unit = {
    ConfigParser.parse(Array())
  }
}
