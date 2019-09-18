package uk.gov.ukho.ais.heatmaps.aggregator.strategy

import org.assertj.core.api.SoftAssertions
import org.junit.Test
import uk.gov.ukho.ais.heatmaps.aggregator.model.AggregationType._
import uk.gov.ukho.ais.heatmaps.aggregator.model.{
  AggregationTarget,
  MonthlyS3File
}

import scala.collection.JavaConverters._

class AggregationTargetStrategyTest {

  private val ANNUAL_2000_PATH: String =
    "sqlFilename=sqlFile.sql/resample=resampleval/type=annual/year=2000/sqlFile.sql-1km-res-resampleval-annual-2000.tif"
  private val WINTER_1999_PATH: String =
    "sqlFilename=sqlFile.sql/resample=resampleval/type=seasonal/year=1999/season=winter/sqlFile.sql-1km-res-resampleval-winter-1999.tif"
  private val SPRING_PATH: String =
    "sqlFilename=sqlFile.sql/resample=resampleval/type=seasonal/year=2000/season=spring/sqlFile.sql-1km-res-resampleval-spring-2000.tif"
  private val SUMMER_PATH: String =
    "sqlFilename=sqlFile.sql/resample=resampleval/type=seasonal/year=2000/season=summer/sqlFile.sql-1km-res-resampleval-summer-2000.tif"
  private val AUTUMN_PATH: String =
    "sqlFilename=sqlFile.sql/resample=resampleval/type=seasonal/year=2000/season=autumn/sqlFile.sql-1km-res-resampleval-autumn-2000.tif"
  private val WINTER_2000_PATH: String =
    "sqlFilename=sqlFile.sql/resample=resampleval/type=seasonal/year=2000/season=winter/sqlFile.sql-1km-res-resampleval-winter-2000.tif"

  @Test
  def whenJanToDec2000PassedInThenOutputFilenamesCorrect(): Unit =
    SoftAssertions.assertSoftly { softly =>
      val expectedSeasonalPath: Map[Int, String] = Map(
        1 -> WINTER_1999_PATH,
        2 -> SPRING_PATH,
        3 -> SPRING_PATH,
        4 -> SPRING_PATH,
        5 -> SUMMER_PATH,
        6 -> SUMMER_PATH,
        7 -> SUMMER_PATH,
        8 -> AUTUMN_PATH,
        9 -> AUTUMN_PATH,
        10 -> AUTUMN_PATH,
        11 -> WINTER_2000_PATH,
        12 -> WINTER_2000_PATH
      )

      for (month: Int <- 1 to 12) {
        val monthlyS3File: MonthlyS3File = MonthlyS3File("/some/path/a.tiff",
                                                         "sqlFile.sql",
                                                         "resampleval",
                                                         2000,
                                                         month)

        val outputPaths: List[AggregationTarget] =
          AggregationTargetStrategy.getAggregationTargetsForMonthlyFile(
            monthlyS3File)

        softly
          .assertThat(outputPaths.iterator.asJava)
          .usingFieldByFieldElementComparator()
          .containsExactlyInAnyOrder(
            AggregationTarget(expectedSeasonalPath(month),
                              SEASONAL,
                              monthlyS3File),
            AggregationTarget(ANNUAL_2000_PATH, ANNUAL, monthlyS3File)
          )
      }
    }

}
