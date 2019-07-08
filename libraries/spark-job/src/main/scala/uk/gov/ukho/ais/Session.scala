package uk.gov.ukho.ais

import org.apache.spark.sql.SparkSession

object Session {
  private var sparkSessionOption: Option[SparkSession] = Option.empty

  def init(applicationName: String, isTestSession: Boolean = false): Unit = {
    def buildTestSession: SparkSession = {
      SparkSession
        .builder()
        .master("local[*]")
        .appName(s"Test session for: $applicationName")
        .getOrCreate()
    }

    def buildSession: SparkSession = {
      SparkSession
        .builder()
        .appName(applicationName)
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
