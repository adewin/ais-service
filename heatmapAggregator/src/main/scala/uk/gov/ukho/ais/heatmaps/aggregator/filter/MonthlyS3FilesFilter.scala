package uk.gov.ukho.ais.heatmaps.aggregator.filter

import uk.gov.ukho.ais.heatmaps.aggregator.model.{MonthlyS3File, S3File}

import scala.util.matching.Regex

object MonthlyS3FilesFilter {

  implicit class Filter(files: List[S3File]) {
    private final val MONTHLY_REGEX: Regex = ("^sqlFilename=(.*)" +
      "\\/resample=(.*)" +
      "\\/type=monthly" +
      "\\/year=([0-9]{4})" +
      "\\/month=(1[0-2]|[1-9])" +
      "\\/.*\\.tif$").r

    def filterMonthlyS3Files: List[MonthlyS3File] = {
      files
        .map(_.path)
        .collect {
          case path @ MONTHLY_REGEX(sqlFilename, resample, year, month) =>
            MonthlyS3File(path, sqlFilename, resample, year.toInt, month.toInt)
        }
    }
  }

}
