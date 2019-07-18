package uk.gov.ukho.ais.partitioning

import scopt.OParser

case class Config(inputPath: String, outputDirectory: String) {}

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
        .action((value, config) => config.copy(outputDirectory = value))
    )
  }

  def parse(args: Array[String]): Config =
    OParser
      .parse(
        PARSER,
        args,
        Config(
          inputPath = "",
          outputDirectory = ""
        )
      )
      .fold {
        throw new IllegalStateException("config not loaded")
      }(identity)
}
