package uk.gov.ukho.ais.heatmaps.aggregator

import scopt.{OParser, OParserBuilder}

object ConfigParser {

  @transient private final val PARSER = {
    val builder: OParserBuilder[Config] = OParser.builder[Config]

    import builder._

    OParser.sequence(
      programName("heatmaps-aggregate"),
      head("Heatmaps Aggregate",
           "Sums monthly heatmaps into seasonal and yearly heatmaps"),
      help('h', "help"),
      opt[String]('b', "heatmaps-bucket")
        .required()
        .valueName("<bucket-name>")
        .text("bucket name where the heatmaps are stored")
        .action((value, config) => config.copy(heatmapsDirectory = value))
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
