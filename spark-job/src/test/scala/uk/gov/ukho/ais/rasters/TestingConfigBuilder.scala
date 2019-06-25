package uk.gov.ukho.ais.rasters

import scala.collection.mutable

case class TestingConfigBuilder(
    inputPath: String = "in",
    outputDirectory: String = "out",
    draughtConfigFile: String = "draught",
    staticDataFile: String = "static",
    outputFilenamePrefix: String = "prefix",
    resolution: String = "1",
    interpolationTimeThresholdMilliseconds: String = "12",
    interpolationDistanceThresholdMeters: String = "13",
    startPeriod: String = "2019-01-01",
    endPeriod: String = "2019-12-31",
    draughtIndex: Option[String] = Option.empty) {

  def draughtIndex(draughtIndex: String): TestingConfigBuilder = {
    this.copy(draughtIndex = Option(draughtIndex))
  }

  def interpolationTimeThresholdMilliseconds(
      interpolationTimeThresholdMilliseconds: String): TestingConfigBuilder = {
    this.copy(
      interpolationTimeThresholdMilliseconds =
        interpolationTimeThresholdMilliseconds)
  }

  def interpolationDistanceThresholdMeters(
      interpolationDistanceThresholdMeters: String): TestingConfigBuilder = {
    this.copy(
      interpolationDistanceThresholdMeters =
        interpolationDistanceThresholdMeters)
  }

  def inputPath(inputPath: String): TestingConfigBuilder = {
    this.copy(inputPath = inputPath)
  }

  def outputDirectory(outputDirectory: String): TestingConfigBuilder = {
    this.copy(outputDirectory = outputDirectory)
  }

  def staticDataFile(staticDataFile: String): TestingConfigBuilder = {
    this.copy(staticDataFile = staticDataFile)
  }

  def draughtConfigFile(draughtConfigFile: String): TestingConfigBuilder = {
    this.copy(draughtConfigFile = draughtConfigFile)
  }

  def startPeriod(startPeriod: String): TestingConfigBuilder = {
    this.copy(startPeriod = startPeriod)
  }

  def endPeriod(endPeriod: String): TestingConfigBuilder = {
    this.copy(endPeriod = endPeriod)
  }

  def build(): Array[String] = {
    val args: mutable.MutableList[String] = mutable.MutableList(
      "-i",
      inputPath,
      "-o",
      outputDirectory,
      "-p",
      outputFilenamePrefix,
      "-r",
      resolution,
      "-t",
      interpolationTimeThresholdMilliseconds,
      "-d",
      interpolationDistanceThresholdMeters,
      "-s",
      startPeriod,
      "-e",
      endPeriod,
      "--draughtConfigFile",
      draughtConfigFile,
      "--staticDataFile",
      staticDataFile,
      "-l"
    )

    draughtIndex.fold {} { draughtIndex =>
      args ++= Seq("--draughtIndex", draughtIndex)
    }

    args.toArray
  }
}

object TestingConfigBuilder {

  def fromDefaults(): TestingConfigBuilder = {
    new TestingConfigBuilder()
  }
}
