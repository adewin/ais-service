package uk.gov.ukho.ais.resample

import org.apache.spark.sql.SaveMode
import uk.gov.ukho.ais.resample.RDDToDataFrameConverter.Converter
import uk.gov.ukho.ais.resample.Resampler.RDDResampler
import uk.gov.ukho.ais.resample.ShipPingConverter.ConvertToShipPingTuple
import uk.gov.ukho.ais.{Schema, Session}

object ResampleAis {

  def main(args: Array[String]): Unit = {
    Session.init("Resample AIS")

    implicit val config: Config = ConfigParser.parse(args)

    val selectClause: Seq[String] =
      Schema.PARTITIONED_AIS_SCHEMA.map(field => field.name)

    Session.sparkSession.read
      .schema(Schema.PARTITIONED_AIS_SCHEMA)
      .option("sep", "\t")
      .csv(config.inputPath)
      .selectExpr(selectClause: _*)
      .rdd
      .convertToKeyedTuple()
      .resample
      .convertToDataFrame()
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
