package uk.gov.ukho.ais.heatmaps.generator

case class Config(athenaRegion: String,
                  athenaResultsBucket: String,
                  filterSqlFile: String,
                  outputDirectory: String,
                  outputFilePrefix: String,
                  isLocal: Boolean,
                  interpolationTimeThresholdMilliseconds: Long,
                  interpolationDistanceThresholdMeters: Long,
                  resolution: Double,
                  year: Int,
                  month: Int)

object Config {
  val default = Config(
    athenaRegion = "eu-west-2",
    athenaResultsBucket = "ukho-data-query-results",
    filterSqlFile = "",
    outputDirectory = ".",
    outputFilePrefix = "",
    isLocal = false,
    interpolationTimeThresholdMilliseconds = 0,
    interpolationDistanceThresholdMeters = 0,
    resolution = 1,
    year = 0,
    month = 0
  )
}
