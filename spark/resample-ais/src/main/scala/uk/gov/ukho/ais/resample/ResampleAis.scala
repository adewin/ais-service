package uk.gov.ukho.ais.resample

import org.apache.spark.sql.SaveMode
import uk.gov.ukho.ais.resample.ResamplingFilter.ResampleAisPings
import uk.gov.ukho.ais.{Schema, Session}

object ResampleAis {

  def main(args: Array[String]): Unit = {

    Session.init("Resample AIS")

    implicit val config: Config = ConfigParser.parse(args)

    val selectClause: Seq[String] =
      Schema.MINIMAL_PARTITIONED_AIS_SCHEMA.map(field => field.name)

    val step1 = Session.sparkSession.read
      .schema(Schema.PARTITIONED_AIS_SCHEMA)
      .option("sep", "\t")
      .csv(config.inputPath)
      .selectExpr(selectClause: _*)

    val step2 = step1.resample

    step2
      .write
      .partitionBy("year", "month", "day")
      .format("csv")
      .option("sep", "\t")
      .option("timestampFormat", "yyyy-MM-dd HH:mm:ss")
      .option("compression", "bzip2")
      .mode(SaveMode.Append)
      .save(config.outputDirectory)
  }
}
