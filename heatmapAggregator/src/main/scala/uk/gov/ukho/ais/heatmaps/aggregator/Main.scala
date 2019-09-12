package uk.gov.ukho.ais.heatmaps.aggregator

import com.amazonaws.services.s3.{AmazonS3, AmazonS3ClientBuilder}
import uk.gov.ukho.ais.heatmaps.aggregator.service.AggregationOrchestrationService

object Main {

  def main(args: Array[String]): Unit = {
    implicit val config: Config = ConfigParser.parse(args)
    implicit val s3Client: AmazonS3 = AmazonS3ClientBuilder.defaultClient()

    AggregationOrchestrationService.orchestrateAggregation
  }

}
