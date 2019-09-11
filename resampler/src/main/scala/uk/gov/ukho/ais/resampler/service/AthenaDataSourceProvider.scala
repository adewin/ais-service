package uk.gov.ukho.ais.resampler.service

import javax.sql.DataSource
import uk.gov.ukho.ais.resampler.Config

object AthenaDataSourceProvider {
  val DRIVER_CLASS: String = "com.simba.athena.jdbc.Driver"

  def dataSource(implicit config: Config): DataSource = {
    Class.forName(DRIVER_CLASS)

    val JDBC_CONNECTION_URL
      : String = s"jdbc:awsathena://AwsRegion=${config.athenaRegion};" +
      s"S3OutputLocation=s3://${config.athenaResultsBucket};" +
      "AWSCredentialsProviderClass=com.amazonaws.auth.DefaultAWSCredentialsProviderChain"

    val dataSource = new com.simba.athena.jdbc.DataSource()
    dataSource.setURL(JDBC_CONNECTION_URL)

    dataSource
  }
}
