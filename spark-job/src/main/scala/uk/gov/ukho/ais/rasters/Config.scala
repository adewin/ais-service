package uk.gov.ukho.ais.rasters

import scopt.OParser

case class Config(inputPath: String,
                  outputDirectory: String,
                  outputFilenamePrefix: String,
                  isLocal: Boolean,
                  resolution: Double,
                  interpolationTimeThresholdMilliseconds: Long,
                  interpolationDistanceThresholdMeters: Long)
object Config {

  private val PARSER = {
    val builder = OParser.builder[Config]

    def validatePositive: AnyVal => Either[String, Unit] = {
      case value: Double if value > 0 => builder.success
      case value: Long if value > 0   => builder.success
      case _                          => builder.failure("Negative values are not allowed")
    }

    import builder._
    OParser.sequence(
      programName("ais-to-raster"),
      head("AIS to Raster",
           "Generates a global raster in GeoTiff and PNG formats using WGS84"),
      help('h', "help"),
      opt[String]('i', "input")
        .required()
        .valueName("<input data path/S3 URI>")
        .text("path of input data")
        .action((value, config) => config.copy(inputPath = value)),
      opt[String]('o', "output")
        .required()
        .valueName("<output directory/S3 bucket>")
        .text("location to output the resulting GeoTIFF and PNG")
        .action((value, config) => config.copy(outputDirectory = value)),
      opt[Double]('r', "resolution")
        .required()
        .valueName("<decimal degrees>")
        .text("cell size for resulting raster")
        .action((value, config) => config.copy(resolution = value))
        .validate(validatePositive),
      opt[Long]('t', "timeThreshold")
        .required()
        .valueName("<milliseconds>")
        .text("threshold of time between pings in which interpolation will not occur")
        .action((value, config) =>
          config.copy(interpolationTimeThresholdMilliseconds = value))
        .validate(validatePositive),
      opt[Long]('d', "distanceThreshold")
        .required()
        .valueName("<meters>")
        .text("threshold of distance between pings in which interpolation will not occur")
        .action((value, config) =>
          config.copy(interpolationDistanceThresholdMeters = value))
        .validate(validatePositive),
      opt[String]('p', "prefix")
        .required()
        .valueName("<output filename prefix>")
        .text("prefix to be added to the output filename")
        .action((value, config) => config.copy(outputFilenamePrefix = value)),
      opt[Unit]('l', "local-mode")
        .optional()
        .text("run in local mode")
        .action((_, config) => config.copy(isLocal = true))
    )
  }

  def parse(args: Array[String]): Option[Config] =
    OParser.parse(
      PARSER,
      args,
      Config(
        inputPath = "",
        outputDirectory = "",
        outputFilenamePrefix = "",
        isLocal = false,
        resolution = 1,
        interpolationTimeThresholdMilliseconds = 0,
        interpolationDistanceThresholdMeters = 0
      )
    )
}
