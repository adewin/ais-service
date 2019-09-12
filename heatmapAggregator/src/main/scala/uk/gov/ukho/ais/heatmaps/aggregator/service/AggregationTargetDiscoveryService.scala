package uk.gov.ukho.ais.heatmaps.aggregator.service

import uk.gov.ukho.ais.heatmaps.aggregator.model.AggregationType._
import uk.gov.ukho.ais.heatmaps.aggregator.model.{
  AggregationTarget,
  MonthlyS3File,
  S3File
}
import uk.gov.ukho.ais.heatmaps.aggregator.strategy.AggregationTargetStrategy

object AggregationTargetDiscoveryService {

  def discoverAggregationTargets(
      monthlyS3Files: List[MonthlyS3File],
      filesInBucket: List[S3File]): Map[String, List[String]] = {
    monthlyS3Files
      .flatMap(AggregationTargetStrategy.getAggregationTargetsForMonthlyFile)
      .groupBy(target => (target.path, target.aggregationType))
      .filter {
        case ((path: String, aggregationType: AggregationType),
              targets: List[AggregationTarget]) =>
          targets.size == aggregationType.id && !filesInBucket
            .map(_.path)
            .contains(path)
      }
      .map {
        case ((path: String, _: AggregationType),
              targets: List[AggregationTarget]) =>
          (path, targets.map(_.file.path))
      }
  }

}
