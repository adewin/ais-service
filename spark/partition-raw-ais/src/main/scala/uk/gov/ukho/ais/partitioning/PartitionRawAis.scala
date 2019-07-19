package uk.gov.ukho.ais.partitioning

import org.apache.spark.sql.SaveMode
import org.apache.spark.sql.functions.input_file_name
import uk.gov.ukho.ais.{Schema, Session}

object PartitionRawAis {
  private val YEAR_EXPRESSION = "year(acquisition_time) as year"
  private val MONTH_EXPRESSION = "month(acquisition_time) as month"
  private val DAY_EXPRESSION = "day(acquisition_time) as day"

  def main(args: Array[String]): Unit = {
    Session.init("Partition Raw AIS")

    implicit val config: Config = ConfigParser.parse(args)

    val selectClause: Seq[String] =
      Schema.AIS_SCHEMA.map(field => field.name) ++ Seq(YEAR_EXPRESSION,
                                                        MONTH_EXPRESSION,
                                                        DAY_EXPRESSION)

    Session.sparkSession.read
      .schema(Schema.AIS_SCHEMA)
      .option("sep", "\t")
      .csv(config.inputPath)
      .selectExpr(selectClause: _*)
      .withColumn("input_ais_data_file", input_file_name())
      .write
      .partitionBy("year", "month", "day")
      .format("csv")
      .option("sep", "\t")
      .option("compression", "bzip2")
      .option("timestampFormat", "yyyy-MM-dd HH:mm:ss")
      .mode(SaveMode.Append)
      .save(config.outputDirectory)
  }
}
