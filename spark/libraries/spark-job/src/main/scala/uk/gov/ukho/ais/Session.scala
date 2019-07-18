package uk.gov.ukho.ais

import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession

object Session {
  private var sparkSessionOption: Option[SparkSession] = Option.empty

  def init(applicationName: String, isTestSession: Boolean = false): Unit = {
    def commonConfig: SparkConf = {
      new SparkConf()
        .setAll(
          Array(
            ("spark.sql.session.timeZone", "UTC"),
            ("spark.driver.extraJavaOptions", "-Duser.timezone=UTC"),
            ("spark.executor.extraJavaOptions", "-Duser.timezone=UTC"),
            ("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
          ))
    }

    def buildTestSession: SparkSession = {
      SparkSession
        .builder()
        .master("local[*]")
        .appName(s"Test session for: $applicationName")
        .config(commonConfig)
        .getOrCreate()
    }

    def buildSession: SparkSession = {
      SparkSession
        .builder()
        .appName(applicationName)
        .config(commonConfig)
        .getOrCreate()
    }

    sparkSessionOption = Option(
      if (isTestSession) buildTestSession else buildSession)
  }

  def sparkSession: SparkSession =
    sparkSessionOption.fold {
      throw new IllegalStateException("No spark session")
    } {
      identity
    }
}
