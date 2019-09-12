package uk.gov.ukho.ais.heatmaps.aggregator.service

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.{ObjectListing, S3ObjectSummary}
import org.assertj.core.api.SoftAssertions
import org.junit.{Before, Test}
import org.mockito.IdiomaticMockito
import org.mockito.Mockito._
import uk.gov.ukho.ais.heatmaps.aggregator.Config
import uk.gov.ukho.ais.heatmaps.aggregator.model.S3File

import scala.collection.JavaConverters._
import scala.collection.mutable

class S3FileServiceTest extends IdiomaticMockito {

  private val s3ClientMock: AmazonS3 = mock[AmazonS3]
  private val configMock: Config = mock[Config]
  private val objectListingMock: ObjectListing = mock[ObjectListing]
  private val file1: S3ObjectSummary = mock[S3ObjectSummary]
  private val file2: S3ObjectSummary = mock[S3ObjectSummary]

  private final val HEATMAP_BUCKET: String = "bucket1"
  private final val FILE1_KEY: String = "blah/blah/file1.tif"
  private final val FILE2_KEY: String = "blah/blah/file2.tif"

  @Before
  def setup(): Unit = {
    when(configMock.heatmapsDirectory) thenReturn HEATMAP_BUCKET
    when(file1.getKey) thenReturn FILE1_KEY
    when(file2.getKey) thenReturn FILE2_KEY
    when(objectListingMock.getObjectSummaries) thenReturn mutable
      .Buffer(file1, file2)
      .asJava
    when(s3ClientMock.listObjects(HEATMAP_BUCKET)) thenReturn objectListingMock
  }

  @Test
  def whenGetFilesThenReturnedAsListOfString(): Unit =
    SoftAssertions.assertSoftly { softly =>
      val files: List[S3File] =
        S3FileService.retrieveFileList(configMock, s3ClientMock)

      softly
        .assertThat(files.iterator.asJava)
        .usingFieldByFieldElementComparator()
        .containsExactly(S3File(FILE1_KEY), S3File(FILE2_KEY))
    }
}
