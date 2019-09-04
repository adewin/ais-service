package uk.gov.ukho.ais.partitioning

import scopt.OParser

case class Config(inputPath: String,
                  outputDirectory: String,
                  athenaRegion: String,
                  athenaResultsBucket: String,
                  outputDatabase: Option[String],
                  outputTable: Option[String])

object ConfigParser {
  @transient private final val PARSER = {
    val builder = OParser.builder[Config]

    import builder._
    OParser.sequence(
      programName("partition-raw-ais"),
      head("Partition Raw AIS",
           "Partitions the raw AIS data by month and year"),
      help('h', "help"),
      opt[String]('i', "input")
        .required()
        .valueName("<input data path/S3 URI>")
        .text("path of input data")
        .action((value, config) => config.copy(inputPath = value)),
      opt[String]('o', "output")
        .required()
        .valueName("<output directory/S3 bucket>")
        .text("location to output the resulting partitioned files")
        .action((value, config) => config.copy(outputDirectory = value)),
      opt[String]('a', "athena-region")
        .valueName("<region identifier>")
        .text("region in which Athena queries will execute")
        .action((value, config) => config.copy(athenaRegion = value)),
      opt[String]('b', "athena-results-bucket")
        .valueName("<S3 bucket>")
        .text("bucket for storing Athena query results")
        .action((value, config) => config.copy(athenaResultsBucket = value)),
      opt[String]('d', "database")
        .optional()
        .valueName("<output Athena database>")
        .text("output Athena database to update partition metadata for")
        .action((value, config) => config.copy(outputDatabase = Some(value))),
      opt[String]('t', "table")
        .optional()
        .valueName("<output Athena table>")
        .text("output Athena table to update partition metadata for")
        .action((value, config) => config.copy(outputTable = Some(value)))
    )
  }

  def parse(args: Array[String]): Config =
    OParser
      .parse(
        PARSER,
        args,
        Config(
          inputPath = "",
          athenaRegion = "eu-west-2",
          athenaResultsBucket = "ukho-data-query-results",
          outputDirectory = "",
          outputDatabase = None,
          outputTable = None
        )
      )
      .fold {
        throw new IllegalStateException("config not loaded")
      }(identity)
}
