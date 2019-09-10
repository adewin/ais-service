package uk.gov.ukho.ais.resampler

import scopt.OParser

object ConfigParser {

  @transient private final val PARSER = {
    val builder = OParser.builder[Config]

    def validatePositive: AnyVal => Either[String, Unit] = {
      case value: Double if value > 0 => builder.success
      case value: Int if value > 0    => builder.success
      case value: Long if value > 0   => builder.success
      case _                          => builder.failure("Negative values are not allowed")
    }

    import builder._

    OParser.sequence(
      programName("ais-to-raster"),
      head("AIS to Raster",
           "Generates a global raster in GeoTiff and PNG formats using WGS84"),
      help('h', "help"),
      opt[String]('a', "athena-region")
        .valueName("<region identifier>")
        .text("region in which Athena queries will execute")
        .action((value, config) => config.copy(athenaRegion = value)),
      opt[String]('b', "athena-results-bucket")
        .valueName("<S3 bucket>")
        .text("bucket for storing Athena query results")
        .action((value, config) => config.copy(athenaResultsBucket = value)),
      opt[String]('s', "database")
        .required()
        .valueName("<database>")
        .text("database holding input AIS pings")
        .action((value, config) => config.copy(database = value)),
      opt[String]('l', "table")
        .required()
        .valueName("<table>")
        .text("table holding input AIS pings")
        .action((value, config) => config.copy(table = value)),
      opt[String]('o', "output")
        .required()
        .valueName("<output directory/S3 bucket>")
        .text("location to output the resulting resampled data")
        .action((value, config) => config.copy(outputDirectory = value)),
      opt[String]('p', "prefix")
        .valueName("<output filename prefix>")
        .text("prefix to prepend to filenames")
        .action((value, config) => config.copy(outputFilePrefix = value)),
      opt[Unit]('l', "local-mode")
        .optional()
        .text("run in local mode")
        .action((_, config) => config.copy(isLocal = true)),
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
      arg[String]("<file>...")
        .unbounded()
        .required()
        .text("raw data files for which resampling should be performed")
        .action((value, config) =>
          config.copy(inputFiles = config.inputFiles :+ value))
    )
  }

  def parse(args: Array[String]): Config =
    OParser
      .parse(
        PARSER,
        args,
        Config.default
      )
      .fold {
        throw new IllegalStateException("config not loaded")
      }(identity)
}
