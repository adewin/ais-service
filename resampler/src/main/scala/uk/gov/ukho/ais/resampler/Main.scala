package uk.gov.ukho.ais.resampler

import com.amazonaws.services.s3.{AmazonS3, AmazonS3ClientBuilder}
import javax.sql.DataSource
import uk.gov.ukho.ais.resampler.service.AthenaDataSourceProvider

object Main {

  def main(args: Array[String]): Unit = {
    implicit val amazonS3: AmazonS3 = AmazonS3ClientBuilder.defaultClient()
    implicit val config: Config = ConfigParser.parse(args)

    implicit val dataSource: DataSource = AthenaDataSourceProvider.dataSource

    ResamplerOrchestrator.orchestrateResampling()
  }
}
