package uk.gov.ukho.ais.rasters

import org.apache.spark.sql.SparkSession

object Session {
  private var sparkSessionOption: Option[SparkSession] = Option.empty

  def init(isTestSession: Boolean = false): Unit = {
    def buildTestSession = {
      SparkSession
        .builder()
        .master("local[*]")
        .appName("Spark Testing")
        .getOrCreate()
    }

    def buildSession = {
      SparkSession
        .builder()
        .appName("AIS to Raster")
        .config("spark.serializer",
                "org.apache.spark.serializer.KryoSerializer")
        .getOrCreate()
    }

    sparkSessionOption = Option(
      if (isTestSession) buildTestSession else buildSession)
  }

  def sparkSession: SparkSession =
    sparkSessionOption.fold {
      throw new IllegalStateException("No spark session")
    } { sparkSession =>
      sparkSession
    }
}
