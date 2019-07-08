package uk.gov.ukho.ais.rasters

import java.sql.Timestamp
import java.time.LocalDate
import java.time.format.DateTimeFormatter

import scopt.OParser

import scala.util.Try

case class Config(inputPath: String,
                  outputDirectory: String,
                  draughtConfigFile: String,
                  staticDataFile: String,
                  outputFilenamePrefix: String,
                  isLocal: Boolean,
                  resolution: Double,
                  interpolationTimeThresholdMilliseconds: Long,
                  interpolationDistanceThresholdMeters: Long,
                  startPeriod: Timestamp,
                  endPeriod: Timestamp,
                  draughtIndex: Option[Int]) {}

object ConfigParser {
  private final val NO_TIMESTAMP_SUPPLIED =
    Timestamp.valueOf("1970-01-01 00:00:00")

  @transient private final val PARSER = {
    val builder = OParser.builder[Config]

    def validatePositive: AnyVal => Either[String, Unit] = {
      case value: Double if value > 0 => builder.success
      case value: Long if value > 0   => builder.success
      case _                          => builder.failure("Negative values are not allowed")
    }

    def validateDate: String => Either[String, Unit] = {
      case value if isValidDate(value) => builder.success
      case _ =>
        builder.failure(
          "Invalid format, date must be in format of: 'YYYY-MM-DD'")
    }

    def validateDraughtIndex: String => Either[String, Unit] = {
      case value if value == "unknown" => builder.success
      case value if Try(value.toInt).isSuccess && value.toInt >= 0 =>
        builder.success
      case _ =>
        builder.failure(
          "draughtIndex must be 'unknown' or a numeric and positive integer")
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
      opt[String]("draughtConfigFile")
        .required()
        .valueName("<file containing the draught buckets to use>")
        .text("csv file ov min,max draught values representing the buckets to use for draught variants")
        .action((value, config) => config.copy(draughtConfigFile = value)),
      opt[String]("staticDataFile")
        .required()
        .valueName("<file containing the Arkevista static AIS data>")
        .text("tsv file provided by Arkevista containing static AIS data")
        .action((value, config) => config.copy(staticDataFile = value)),
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
      opt[String]('s', "startPeriod")
        .required()
        .valueName("<startPeriod>")
        .text(
          "start date when to count the first ping from, as an ISO format date")
        .action((value, config) =>
          config.copy(startPeriod = TimestampConverter
            .convertToTimestamp(value, forStartPeriod = true)))
        .validate(validateDate),
      opt[String]('e', "endPeriod")
        .required()
        .valueName("<endPeriod>")
        .text("end date when to count the pings up to, as an ISO format date")
        .action((value, config) =>
          config.copy(endPeriod = TimestampConverter
            .convertToTimestamp(value, forStartPeriod = false)))
        .validate(validateDate),
      opt[String]('p', "prefix")
        .required()
        .valueName("<output filename prefix>")
        .text("prefix to be added to the output filename")
        .action((value, config) => config.copy(outputFilenamePrefix = value)),
      opt[String]("draughtIndex")
        .optional()
        .valueName("<draught range index>")
        .text("index of the draught range")
        .action((value, config) =>
          config.copy(draughtIndex = determineDraughtIndex(value)))
        .validate(validateDraughtIndex),
      opt[Unit]('l', "local-mode")
        .optional()
        .text("run in local mode")
        .action((_, config) => config.copy(isLocal = true))
    )
  }

  private def determineDraughtIndex(value: String): Option[Int] = {
    Option(if (value == "unknown") {
      -1
    } else {
      value.toInt
    })
  }

  def parse(args: Array[String]): Config =
    OParser
      .parse(
        PARSER,
        args,
        Config(
          inputPath = "",
          outputDirectory = "",
          draughtConfigFile = "",
          staticDataFile = "",
          outputFilenamePrefix = "",
          isLocal = false,
          resolution = 1,
          interpolationTimeThresholdMilliseconds = 0,
          interpolationDistanceThresholdMeters = 0,
          startPeriod = NO_TIMESTAMP_SUPPLIED,
          endPeriod = NO_TIMESTAMP_SUPPLIED,
          draughtIndex = None
        )
      )
      .fold { throw new IllegalStateException("config not loaded") }(identity)

  private def isValidDate(date: String): Boolean = {
    Try(LocalDate.parse(date, DateTimeFormatter.ISO_DATE)).isSuccess
  }
}
