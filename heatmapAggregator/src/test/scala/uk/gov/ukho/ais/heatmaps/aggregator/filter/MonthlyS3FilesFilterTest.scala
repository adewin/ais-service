package uk.gov.ukho.ais.heatmaps.aggregator.filter

import org.assertj.core.api.SoftAssertions
import org.junit.Test
import uk.gov.ukho.ais.heatmaps.aggregator.model.{MonthlyS3File, S3File}

import scala.collection.JavaConverters._

class MonthlyS3FilesFilterTest {

  @Test
  def whenNoMonthlyFilesThenFilterReturnsNoMonthlyS3Files(): Unit =
    SoftAssertions.assertSoftly { softly =>
      val inputFiles: List[S3File] = List(
        S3File(
          "sqlFilename=mydile.sql/resample=6hr-30km/type=yearly/year=2019/2019yr-6hr,tif"),
        S3File(
          "sqlFilename=mydile.sql/resample=6hr-30km/type=seasonal/year=2018/season=winter/2019wi-6hr,tif")
      )

      val filteredFiles: List[MonthlyS3File] =
        MonthlyS3FilesFilter.Filter(inputFiles).filterMonthlyS3Files

      softly.assertThat(filteredFiles.iterator.asJava).isEmpty()
    }

  @Test
  def whenTwoMonthlyFilesThenFilterReturnsTwoMonthlyS3FilesWithCorrectYearsAndMonths()
    : Unit =
    SoftAssertions.assertSoftly { softly =>
      val inputFiles: List[S3File] = List(
        S3File(
          "sqlFilename=myfile.sql/resample=6hr-30km/type=monthly/year=2019/month=2/2019-02mo-6hr.tif"),
        S3File(
          "sqlFilename=myfile.sql/resample=6hr-30km/type=yearly/year=2019/2019yr-6hr.tif"),
        S3File(
          "sqlFilename=myfile.sql/resample=6hr-30km/type=monthly/year=2018/month=3/2018-03mo-6hr.tif"),
        S3File(
          "sqlFilename=myfile.sql/resample=6hr-30km/type=yearly/year=2019/month=2/2019yr-6hr-wrong.tif")
      )

      val filteredFiles: List[MonthlyS3File] =
        MonthlyS3FilesFilter.Filter(inputFiles).filterMonthlyS3Files

      softly
        .assertThat(filteredFiles.iterator.asJava)
        .usingFieldByFieldElementComparator()
        .containsExactly(
          MonthlyS3File(
            "sqlFilename=myfile.sql/resample=6hr-30km/type=monthly/year=2019/month=2/2019-02mo-6hr.tif",
            "myfile.sql",
            "6hr-30km",
            2019,
            2),
          MonthlyS3File(
            "sqlFilename=myfile.sql/resample=6hr-30km/type=monthly/year=2018/month=3/2018-03mo-6hr.tif",
            "myfile.sql",
            "6hr-30km",
            2018,
            3)
        )
    }
}
