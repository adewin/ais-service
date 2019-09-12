package uk.gov.ukho.ais.resampler

case class Config(athenaRegion: String,
                  database: String,
                  table: String,
                  inputFiles: Seq[String],
                  outputDirectory: String,
                  outputFilePrefix: String,
                  isLocal: Boolean,
                  interpolationTimeThresholdMilliseconds: Long,
                  interpolationDistanceThresholdMeters: Long,
                  resolution: Double)

object Config {
  val default = Config(
    athenaRegion = "eu-west-2",
    database = "",
    table = "",
    inputFiles = Seq.empty,
    outputDirectory = ".",
    outputFilePrefix = "",
    isLocal = false,
    interpolationTimeThresholdMilliseconds = 0,
    interpolationDistanceThresholdMeters = 0,
    resolution = 1
  )
}
