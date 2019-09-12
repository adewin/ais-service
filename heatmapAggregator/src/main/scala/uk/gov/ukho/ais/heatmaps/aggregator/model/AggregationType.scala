package uk.gov.ukho.ais.heatmaps.aggregator.model

object AggregationType extends Enumeration {
  type AggregationType = Value
  val ANNUAL: AggregationType = Value(12)
  val SEASONAL: AggregationType = Value(3)
}
