package uk.gov.ukho.ais.rasters

import scopt.OParser

case class Config(inputPath: String = "", outputDirectory: String = "", isLocal: Boolean = false, resolution: Double = 1)

object Config {
  private val PARSER = {
    val builder = OParser.builder[Config]
    import builder._
    OParser.sequence(
      programName("ais-to-raster"),
      head("AIS to Raster", "Generates a global raster in GeoTiff and PNG formats using WGS84"),
      help('h', "help"),
      opt[String]('i', "input")
        .required()
        .valueName("<input data path/S3 URI>")
        .action((value, config) => config.copy(inputPath = value))
        .text("path of input data"),
      opt[String]('o', "output")
        .required()
        .valueName("<output directory/S3 bucket>")
        .action((value, config) => config.copy(outputDirectory = value))
        .text("location to output the resulting GeoTIFF and PNG"),
      opt[Unit]('l', "local-mode")
        .optional()
        .action((_, config) => config.copy(isLocal = true))
        .text("run in local mode"),
      opt[Double]('r', "resolution")
        .required()
        .valueName("<decimal degrees>")
        .action((value, config) => config.copy(resolution = value))
        .text("cell size for resulting raster")
        .validate {
          case resolution if resolution > 0 => success
          case _ => failure("")
        }
    )
  }

  def parse(args: Array[String]): Option[Config] =
    OParser.parse(PARSER, args, Config())
}
