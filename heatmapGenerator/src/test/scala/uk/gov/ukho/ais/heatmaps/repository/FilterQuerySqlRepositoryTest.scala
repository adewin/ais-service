package uk.gov.ukho.ais.heatmaps.repository

import java.io.{ByteArrayInputStream, InputStream}
import java.nio.charset.Charset

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.{S3Object, S3ObjectInputStream}
import org.apache.http.client.methods.HttpRequestBase
import org.assertj.core.api.SoftAssertions
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.IdiomaticMockito
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.Mockito._
import uk.gov.ukho.ais.heatmaps.Config

@RunWith(classOf[MockitoJUnitRunner])
class FilterQuerySqlRepositoryTest extends IdiomaticMockito {

  implicit val mockAmazonS3: AmazonS3 = mock[AmazonS3]
  private val mockS3Object: S3Object = mock[S3Object]

  @Test
  def whenCalledWithSqlFileStoredInS3ThenObjectRetrievedFromS3(): Unit =
    SoftAssertions.assertSoftly { softly =>
      val bucketName = "test-bucket"
      val objectName = "object.sql"
      val s3SqlFile = s"s3://$bucketName/$objectName"
      val sql = "SELECT * FROM table"

      implicit val config: Config =
        Config.default.copy(isLocal = false, filterSqlFile = s3SqlFile)

      val inputStream: InputStream =
        new ByteArrayInputStream(sql.getBytes(Charset.defaultCharset()))

      val s3ObjectInputStream: S3ObjectInputStream =
        new S3ObjectInputStream(inputStream, mock[HttpRequestBase])

      when(mockAmazonS3.getObject(bucketName, objectName)) thenReturn mockS3Object
      when(mockS3Object.getObjectContent) thenReturn s3ObjectInputStream

      val filterQuerySqlRepository = new FilterQuerySqlRepository()

      val result = filterQuerySqlRepository.retrieveFilterQuery

      softly.assertThat(result).isEqualTo(sql)
    }
}
