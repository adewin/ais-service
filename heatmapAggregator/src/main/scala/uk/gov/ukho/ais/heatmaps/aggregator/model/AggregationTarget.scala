package uk.gov.ukho.ais.heatmaps.aggregator.model

import uk.gov.ukho.ais.heatmaps.aggregator.model.AggregationType.AggregationType

case class AggregationTarget(path: String,
                             aggregationType: AggregationType,
                             file: MonthlyS3File)
