package uk.gov.ukho.ais.heatmaps.aggregator

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model._
import geotrellis.raster.io.geotiff.SinglebandGeoTiff
import geotrellis.raster.io.geotiff.reader.GeoTiffReader
import org.apache.http.client.methods.HttpRequestBase
import org.assertj.core.api.Assertions._
import org.assertj.core.api.SoftAssertions
import org.junit.{Before, Test}
import org.mockito.ArgumentMatchers._
import org.mockito.IdiomaticMockito
import org.mockito.captor.{ArgCaptor, Captor}
import uk.gov.ukho.ais.heatmaps.aggregator.service.AggregationOrchestrationService

import scala.collection.JavaConverters._
import scala.collection.mutable

class ComponentTest extends IdiomaticMockito {

  private final val EXPECTED_SPRING_PATH: String =
    "sqlFilename=sf/resample=rs/type=seasonal/year=2000/season=spring/sf-1km-res-rs-spring-2000.tif"
  private final val EXPECTED_SUMMER_PATH: String =
    "sqlFilename=sf/resample=rs/type=seasonal/year=2000/season=summer/sf-1km-res-rs-summer-2000.tif"
  private final val EXPECTED_AUTUMN_PATH: String =
    "sqlFilename=sf/resample=rs/type=seasonal/year=2000/season=autumn/sf-1km-res-rs-autumn-2000.tif"
  private final val EXPECTED_ANNUAL_PATH: String =
    "sqlFilename=sf/resample=rs/type=annual/year=2000/sf-1km-res-rs-annual-2000.tif"
  private final val DOUBLE_ACCURACY: Double = 1.0E-5

  private implicit val s3ClientMock: AmazonS3 = mock[AmazonS3]
  private implicit val config: Config = Config("test-directory")
  private val objectListingMock: ObjectListing = mock[ObjectListing]
  private val httpRequestMock: HttpRequestBase = mock[HttpRequestBase]
  private val s3ObjectMock: S3Object = mock[S3Object]
  private val objectMetadata: ObjectMetadata = mock[ObjectMetadata]
  private val badS3Files: Map[String, S3ObjectSummary] =
    Map("badfile1" -> mkFile("badfile1"), "badfile2" -> mkFile("badfile2"))
  private val s3Files: Map[String, S3ObjectSummary] = (1 to 12)
    .map(month =>
      (s"month$month",
       mkFile(
         s"sqlFilename=sf/resample=rs/type=monthly/year=2000/month=$month/m$month.tif")))
    .toMap

  @Before
  def setup(): Unit = {
    s3ClientMock.listObjects(config.heatmapsDirectory) returns objectListingMock
    objectMetadata.getContentLength returns 324
    s3ClientMock.getObjectMetadata(any[GetObjectMetadataRequest]) returns objectMetadata
    s3ClientMock.getObject(any[GetObjectRequest]) returns s3ObjectMock
    s3ObjectMock.getObjectContent answers {
      new S3ObjectInputStream(getClass.getResourceAsStream("/small.tif"),
                              httpRequestMock)
    }
  }

  @Test
  def whenNoAggregationToBeDoneThenNoObjectsPushedToOutput(): Unit = {
    objectListingMock.getObjectSummaries returns mutable
      .Buffer(badS3Files("badfile1"), badS3Files("badfile2"))
      .asJava

    AggregationOrchestrationService.orchestrateAggregation

    s3ClientMock.putObject(any[PutObjectRequest]) wasNever called
  }

  @Test
  def whenSeasonalAggregationToBeDoneThenSeasonObjectPushedToOutput(): Unit =
    SoftAssertions.assertSoftly { softly =>
      objectListingMock.getObjectSummaries returns mutable
        .Buffer(
          s3Files("month2"),
          s3Files("month4"),
          s3Files("month3")
        )
        .asJava

      AggregationOrchestrationService.orchestrateAggregation

      val captor: Captor[PutObjectRequest] = ArgCaptor[PutObjectRequest]
      s3ClientMock.putObject(captor) was called

      softly
        .assertThat(captor.value.getBucketName)
        .isEqualTo(config.heatmapsDirectory)
      softly.assertThat(captor.value.getKey).isEqualTo(EXPECTED_SPRING_PATH)
      val outputTif: SinglebandGeoTiff =
        GeoTiffReader.readSingleband(captor.value.getFile.getAbsolutePath)
      softly
        .assertThat(outputTif.tile.getDouble(0, 0))
        .isCloseTo(0.3, within(DOUBLE_ACCURACY))
      softly
        .assertThat(outputTif.tile.getDouble(3, 3))
        .isCloseTo(2.1, within(DOUBLE_ACCURACY))
    }

  @Test
  def whenSeasonalAggregationToBeDoneButAlreadyExistsThenNothingPushedToOutput()
    : Unit = {
    val springTarget: S3ObjectSummary = new S3ObjectSummary
    springTarget.setBucketName(config.heatmapsDirectory)
    springTarget.setKey(EXPECTED_SPRING_PATH)

    objectListingMock.getObjectSummaries returns mutable
      .Buffer(
        s3Files("month2"),
        s3Files("month4"),
        s3Files("month3"),
        springTarget
      )
      .asJava

    AggregationOrchestrationService.orchestrateAggregation

    s3ClientMock.putObject(any[PutObjectRequest]) wasNever called
  }

  @Test
  def whenYearlyAggregationToBeDoneThenYearlyObjectPushedToOutput(): Unit =
    SoftAssertions.assertSoftly { softly =>
      objectListingMock.getObjectSummaries returns mutable
        .Buffer(
          s3Files("month1"),
          s3Files("month2"),
          s3Files("month3"),
          s3Files("month4"),
          s3Files("month5"),
          s3Files("month6"),
          s3Files("month7"),
          s3Files("month8"),
          s3Files("month9"),
          s3Files("month10"),
          s3Files("month11"),
          s3Files("month12")
        )
        .asJava

      AggregationOrchestrationService.orchestrateAggregation

      val captor: Captor[PutObjectRequest] = ArgCaptor[PutObjectRequest]
      s3ClientMock.putObject(captor) wasCalled fourTimes

      captor.values.foreach { request: PutObjectRequest =>
        softly
          .assertThat(request.getBucketName)
          .isEqualTo(config.heatmapsDirectory)
      }

      softly
        .assertThat(captor.values.map(_.getKey).iterator.asJava)
        .containsExactlyInAnyOrder(
          EXPECTED_SPRING_PATH,
          EXPECTED_SUMMER_PATH,
          EXPECTED_AUTUMN_PATH,
          EXPECTED_ANNUAL_PATH,
        )

      captor.values.foreach { request: PutObjectRequest =>
        softly
          .assertThat(
            GeoTiffReader
              .readSingleband(request.getFile.getAbsolutePath)
              .tile
              .getDouble(0, 0))
          .isEqualTo(if (request.getKey.contains("type=annual")) 1.2 else 0.3,
                     within(DOUBLE_ACCURACY))
      }

      captor.values.foreach { request: PutObjectRequest =>
        softly
          .assertThat(
            GeoTiffReader
              .readSingleband(request.getFile.getAbsolutePath)
              .tile
              .getDouble(3, 3))
          .isEqualTo(if (request.getKey.contains("type=annual")) 8.4 else 2.1,
                     within(DOUBLE_ACCURACY))
      }
    }

  private def mkFile(path: String): S3ObjectSummary = {
    val s3ObjectSummary: S3ObjectSummary = new S3ObjectSummary
    s3ObjectSummary.setBucketName(config.heatmapsDirectory)
    s3ObjectSummary.setKey(path)
    s3ObjectSummary
  }

}
