package uk.gov.ukho.ais.heatmaps

import com.amazonaws.services.s3.{AmazonS3, AmazonS3ClientBuilder}
import uk.gov.ukho.ais.heatmaps.service.AthenaDataSourceProvider

object Main {

  def main(args: Array[String]): Unit = {
    implicit val amazonS3: AmazonS3 = AmazonS3ClientBuilder.defaultClient()
    implicit val config: Config = ConfigParser.parse(args)

    val source = AthenaDataSourceProvider.dataSource

    HeatmapOrchestrator.orchestrateHeatmapGeneration(source)
  }
}
