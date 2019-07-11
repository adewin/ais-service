package uk.gov.ukho.ais.partitioning

import uk.gov.ukho.ais.partitioning.RddWriters.YearMonthPartitionWriter
import uk.gov.ukho.ais.{Schema, Session}

object PartitionRawAis {
  private val YEAR_EXPRESSION = "year(acquisition_time) as year"
  private val MONTH_EXPRESSION = "month(acquisition_time) as month"

  def main(args: Array[String]): Unit = {
    Session.init("Partition Raw AIS")

    implicit val config: Config = ConfigParser.parse(args)

    val selectClause: Seq[String] =
      Schema.AIS_SCHEMA.map(field => field.name) ++ Seq(YEAR_EXPRESSION,
                                                        MONTH_EXPRESSION)

    Session.sparkSession.read
      .schema(Schema.AIS_SCHEMA)
      .option("sep", "\t")
      .csv(config.inputPath)
      .selectExpr(selectClause: _*)
      .rdd
      .keyBy(row => (row.getAs[Int]("year"), row.getAs[Int]("month")))
      .writeYearMonthPartitions()
  }
}
