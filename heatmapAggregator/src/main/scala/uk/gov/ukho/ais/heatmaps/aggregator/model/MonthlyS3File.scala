package uk.gov.ukho.ais.heatmaps.aggregator.model

case class MonthlyS3File(path: String,
                         sqlFilename: String,
                         resample: String,
                         year: Int,
                         month: Int)
