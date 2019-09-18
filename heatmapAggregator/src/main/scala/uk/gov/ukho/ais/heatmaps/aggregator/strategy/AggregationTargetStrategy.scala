package uk.gov.ukho.ais.heatmaps.aggregator.strategy

import org.apache.commons.io.FilenameUtils
import uk.gov.ukho.ais.heatmaps.aggregator.model.AggregationType._
import uk.gov.ukho.ais.heatmaps.aggregator.model.{
  AggregationTarget,
  MonthlyS3File,
  Season
}

object AggregationTargetStrategy {
  private final val RESOLUTION = "1km-res"

  def getAggregationTargetsForMonthlyFile(
      file: MonthlyS3File): List[AggregationTarget] = {
    val season: String = Season.monthToSeason(file.month).toString.toLowerCase
    val seasonYear: Int = if (file.month == 1) file.year - 1 else file.year
    val sqlFilename: String =
      FilenameUtils.getName(file.sqlFilename)
    val seasonalFilename: String =
      s"$sqlFilename-$RESOLUTION-${file.resample}-$season-$seasonYear.tif"
    val annualFilename: String =
      s"$sqlFilename-$RESOLUTION-${file.resample}-annual-${file.year}.tif"

    List(
      AggregationTarget(
        s"sqlFilename=${file.sqlFilename}/resample=${file.resample}/type=annual/year=${file.year}/$annualFilename",
        ANNUAL,
        file),
      AggregationTarget(
        s"sqlFilename=${file.sqlFilename}/resample=${file.resample}/type=seasonal/year=$seasonYear/season=$season/$seasonalFilename",
        SEASONAL,
        file)
    )
  }

}
