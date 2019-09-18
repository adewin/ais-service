package uk.gov.ukho.ais.heatmaps.aggregator.service

import java.lang

import org.assertj.core.api.SoftAssertions
import org.junit.Test
import uk.gov.ukho.ais.heatmaps.aggregator.model.{MonthlyS3File, S3File}

import scala.collection.JavaConverters._

class AggregationTargetDiscoveryServiceTest {

  @Test
  def whenNoFilesFoundThenNoTargetsReturned(): Unit =
    SoftAssertions.assertSoftly { softly =>
      val targets: Map[String, List[String]] = AggregationTargetDiscoveryService
        .discoverAggregationTargets(List(), List())

      softly.assertThat(targets.asJava).isEmpty()
    }

  @Test
  def whenNonTiffFilesOnlyFoundThenNoTargetsReturned(): Unit =
    SoftAssertions.assertSoftly { softly =>
      val filesInBucket: List[S3File] = List(
        makeS3File("myfile.sql", "6hr-30km", "monthly", 2019, 2, "2019-02.bla"),
        makeS3File("myfile.sql", "6hr-30km", "monthly", 2019, 3, "2019-03.bla")
      )

      val targets: Map[String, List[String]] = AggregationTargetDiscoveryService
        .discoverAggregationTargets(List(), filesInBucket)

      softly.assertThat(targets.asJava).isEmpty()
    }

  @Test
  def when2TiffFilesFoundThenNoTargetsReturned(): Unit =
    SoftAssertions.assertSoftly { softly =>
      val monthlyFiles: List[MonthlyS3File] = List(
        makeMonthlyFile("myfile.sql",
                        "6hr-30km",
                        "monthly",
                        2019,
                        2,
                        "2019-02.tif"),
        makeMonthlyFile("myfile.sql",
                        "6hr-30km",
                        "monthly",
                        2019,
                        3,
                        "2019-03.tif")
      )

      val filesInBucket: List[S3File] =
        monthlyFiles.map(file => S3File(file.path))

      val targets: Map[String, List[String]] = AggregationTargetDiscoveryService
        .discoverAggregationTargets(monthlyFiles, filesInBucket)

      softly.assertThat(targets.asJava).isEmpty()
    }

  @Test
  def when3TiffFilesFoundThenSeasonTargetReturned(): Unit =
    SoftAssertions.assertSoftly { softly =>
      val monthlyFiles: List[MonthlyS3File] = List(
        makeMonthlyFile("myfile.sql",
                        "6hr-30km",
                        "monthly",
                        2019,
                        2,
                        "2019-03.tif"),
        makeMonthlyFile("myfile.sql",
                        "6hr-30km",
                        "monthly",
                        2019,
                        3,
                        "2019-04.tif"),
        makeMonthlyFile("myfile.sql",
                        "6hr-30km",
                        "monthly",
                        2019,
                        4,
                        "2019-05.tif")
      )

      val filesInBucket: List[S3File] =
        monthlyFiles.map(file => S3File(file.path))

      val targetPaths: Iterable[String] = AggregationTargetDiscoveryService
        .discoverAggregationTargets(monthlyFiles, filesInBucket)
        .keys

      softly
        .assertThat(targetPaths.asJava)
        .containsExactly(
          "sqlFilename=" +
            "myfile.sql" +
            "/resample=6hr-30km" +
            "/type=seasonal" +
            "/year=2019" +
            "/season=spring" +
            "/myfile.sql-1km-res-6hr-30km-spring-2019.tif")
    }

  @Test
  def when2MonthsDifferentSqlFileThen2SeasonTargetsReturned(): Unit =
    SoftAssertions.assertSoftly { softly =>
      val monthlyFiles: List[MonthlyS3File] = List(
        makeMonthlyFile("myfile.sql",
                        "6hr-30km",
                        "monthly",
                        2018,
                        11,
                        "2018-11.tif"),
        makeMonthlyFile("myfile.sql",
                        "6hr-30km",
                        "monthly",
                        2018,
                        12,
                        "2018-12.tif"),
        makeMonthlyFile("myfile.sql",
                        "6hr-30km",
                        "monthly",
                        2019,
                        1,
                        "2019-01.tif"),
        makeMonthlyFile("myfile2.sql",
                        "6hr-30km",
                        "monthly",
                        2018,
                        11,
                        "2018-11.tif"),
        makeMonthlyFile("myfile2.sql",
                        "6hr-30km",
                        "monthly",
                        2018,
                        12,
                        "2018-12.tif"),
        makeMonthlyFile("myfile2.sql",
                        "6hr-30km",
                        "monthly",
                        2019,
                        1,
                        "2019-01.tif")
      )

      val filesInBucket: List[S3File] =
        monthlyFiles.map(file => S3File(file.path))

      val targetPaths: Iterable[String] = AggregationTargetDiscoveryService
        .discoverAggregationTargets(monthlyFiles, filesInBucket)
        .keys

      softly
        .assertThat(targetPaths.asJava)
        .containsExactlyInAnyOrder(
          "sqlFilename=" +
            "myfile.sql" +
            "/resample=6hr-30km" +
            "/type=seasonal" +
            "/year=2018" +
            "/season=winter" +
            "/myfile.sql-1km-res-6hr-30km-winter-2018.tif",
          "sqlFilename=" +
            "myfile2.sql" +
            "/resample=6hr-30km" +
            "/type=seasonal" +
            "/year=2018" +
            "/season=winter" +
            "/myfile2.sql-1km-res-6hr-30km-winter-2018.tif"
        )
    }

  @Test
  def when2MonthsDifferentResampleThen2SeasonTargetsReturned(): Unit =
    SoftAssertions.assertSoftly { softly =>
      val monthlyFiles: List[MonthlyS3File] = List(
        makeMonthlyFile("myfile.sql",
                        "6hr-30km",
                        "monthly",
                        2018,
                        11,
                        "2018-11.tif"),
        makeMonthlyFile("myfile.sql",
                        "6hr-30km",
                        "monthly",
                        2018,
                        12,
                        "2018-12.tif"),
        makeMonthlyFile("myfile.sql",
                        "6hr-30km",
                        "monthly",
                        2019,
                        1,
                        "2019-01.tif"),
        makeMonthlyFile("myfile.sql",
                        "18hr-100km",
                        "monthly",
                        2018,
                        11,
                        "2018-11.tif"),
        makeMonthlyFile("myfile.sql",
                        "18hr-100km",
                        "monthly",
                        2018,
                        12,
                        "2018-12.tif"),
        makeMonthlyFile("myfile.sql",
                        "18hr-100km",
                        "monthly",
                        2019,
                        1,
                        "2019-01.tif")
      )

      val filesInBucket: List[S3File] =
        monthlyFiles.map(file => S3File(file.path))

      val targetPaths: Iterable[String] = AggregationTargetDiscoveryService
        .discoverAggregationTargets(monthlyFiles, filesInBucket)
        .keys

      softly
        .assertThat(targetPaths.asJava)
        .containsExactlyInAnyOrder(
          "sqlFilename=" +
            "myfile.sql" +
            "/resample=6hr-30km" +
            "/type=seasonal" +
            "/year=2018" +
            "/season=winter" +
            "/myfile.sql-1km-res-6hr-30km-winter-2018.tif",
          "sqlFilename=" +
            "myfile.sql" +
            "/resample=18hr-100km" +
            "/type=seasonal" +
            "/year=2018" +
            "/season=winter" +
            "/myfile.sql-1km-res-18hr-100km-winter-2018.tif"
        )
    }

  @Test
  def when3TiffFilesFoundButAggregateAlreadyExistsThenNoTargetsReturned()
    : Unit =
    SoftAssertions.assertSoftly { softly =>
      val monthlyFiles: List[MonthlyS3File] = List(
        makeMonthlyFile("myfile.sql",
                        "6hr-30km",
                        "monthly",
                        2019,
                        2,
                        "2019-02.tif"),
        makeMonthlyFile("myfile.sql",
                        "6hr-30km",
                        "monthly",
                        2019,
                        3,
                        "2019-03.tif"),
        makeMonthlyFile("myfile.sql",
                        "6hr-30km",
                        "monthly",
                        2019,
                        4,
                        "2019-04.tif")
      )

      val filesInBucket: List[S3File] = monthlyFiles.map(file =>
        S3File(file.path)) ++
        List(
          S3File(
            "sqlFilename=" +
              "myfile.sql" +
              "/resample=6hr-30km" +
              "/type=seasonal" +
              "/year=2019" +
              "/season=spring" +
              "/myfile.sql-1km-res-6hr-30km-spring-2019.tif"))

      val targetPaths: Iterable[String] = AggregationTargetDiscoveryService
        .discoverAggregationTargets(monthlyFiles, filesInBucket)
        .keys

      softly
        .assertThat(targetPaths.asJava)
        .isEmpty()
    }

  private def makeMonthlyFile(sqlFile: String,
                              resample: String,
                              typeName: String,
                              year: Int,
                              month: Int,
                              fileName: String): MonthlyS3File =
    MonthlyS3File(
      makeS3File(sqlFile, resample, typeName, year, month, fileName).path,
      sqlFile,
      resample,
      year,
      month)

  private def makeS3File(sqlFile: String,
                         resample: String,
                         typeName: String,
                         year: Int,
                         month: Int,
                         fileName: String): S3File =
    S3File(
      s"sqlFilename=$sqlFile/resample=$resample/type=$typeName/year=$year/month=$month/$fileName")
}
