package uk.gov.ukho.ais.resampler.repository

import com.amazonaws.services.s3.AmazonS3
import org.apache.commons.io.FilenameUtils
import org.assertj.core.api.SoftAssertions
import org.junit.rules.TemporaryFolder
import org.junit.{Rule, Test}
import org.mockito.IdiomaticMockito
import uk.gov.ukho.ais.resampler.Config
import uk.gov.ukho.ais.resampler.FileUtilities.findGeneratedFiles
import uk.gov.ukho.ais.resampler.model.Ping
import uk.gov.ukho.ais.resampler.utility.TimeUtilities.makeTimestamp

class CsvRepositoryTest extends IdiomaticMockito {

  val _tempDir = new TemporaryFolder()

  @Rule
  def tempDir: TemporaryFolder = _tempDir

  implicit val mockAmazonS3: AmazonS3 = mock[AmazonS3]

  @Test
  def whenYearMonthAndPingsPassedThenCsvCreated(): Unit =
    SoftAssertions.assertSoftly { softly =>
      implicit val config: Config =
        Config.default.copy(outputDirectory = tempDir.getRoot.getAbsolutePath,
                            resolution = 1,
                            isLocal = true)

      val year = 2019
      val month = 1

      val pings = Seq(
        Ping("123", makeTimestamp(0), 179.0, 179.0)
      ).toIterator

      CsvRepository.writePingsForMonth(year, month, pings)

      val files =
        findGeneratedFiles(tempDir.getRoot.getAbsolutePath).map(file =>
          FilenameUtils.getExtension(file))

      softly.assertThat(files).containsExactly("bz2")
    }
}
