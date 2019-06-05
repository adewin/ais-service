package uk.gov.ukho.ais.rasters

import scopt.OParser

case class Config(inputPath: String,
                  outputDirectory: String,
                  outputFilenamePrefix: String,
                  isLocal: Boolean,
                  resolution: Double)

object Config {
  private final val DEFAULT_FILENAME_PREFIX = "world"

  private val PARSER = {
    val builder = OParser.builder[Config]
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
        .validate {
          case resolution if resolution > 0 => success
          case _                            => failure("")
        },
      opt[String]('p', "prefix")
        .optional()
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
      Config(inputPath = "",
             outputDirectory = "",
             outputFilenamePrefix = DEFAULT_FILENAME_PREFIX,
             isLocal = false,
             resolution = 1)
    )
}
