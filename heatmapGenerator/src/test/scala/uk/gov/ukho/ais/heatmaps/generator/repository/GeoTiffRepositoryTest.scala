package uk.gov.ukho.ais.heatmaps.generator.repository

import com.amazonaws.services.s3.AmazonS3
import geotrellis.raster.{CellSize, IntArrayTile, RasterExtent}
import geotrellis.vector.Extent
import org.apache.commons.io.FilenameUtils
import org.assertj.core.api.SoftAssertions
import org.junit.rules.TemporaryFolder
import org.junit.{Rule, Test}
import org.mockito.IdiomaticMockito
import uk.gov.ukho.ais.heatmaps.generator.Config
import uk.gov.ukho.ais.heatmaps.generator.FileUtilities.findGeneratedFiles

class GeoTiffRepositoryTest extends IdiomaticMockito {

  val _tempDir = new TemporaryFolder()

  @Rule
  def tempDir: TemporaryFolder = _tempDir

  implicit val mockAmazonS3: AmazonS3 = mock[AmazonS3]

  @Test
  def whenExtentAndRasterPassedThenPngAndTiffCreated(): Unit =
    SoftAssertions.assertSoftly { softly =>
      implicit val config: Config =
        Config.default.copy(outputDirectory = tempDir.getRoot.getAbsolutePath,
                            resolution = 1,
                            isLocal = true)

      val rasterExtent = RasterExtent(
        Extent(-180, -90, 180, 90).expandBy(config.resolution),
        CellSize(config.resolution, config.resolution)
      )

      val rasterMatrix =
        IntArrayTile.fill(0, rasterExtent.cols, rasterExtent.rows)

      GeoTiffRepository.createOutputFiles((rasterExtent, rasterMatrix))

      val files =
        findGeneratedFiles(tempDir.getRoot.getAbsolutePath).map(file =>
          FilenameUtils.getExtension(file))

      softly.assertThat(files).containsExactly("tif")
    }

  @Test
  def whenExtentAndRasterPassedThenPngAndTiffWithCorrectPrefix(): Unit =
    SoftAssertions.assertSoftly { softly =>
      val outputFilePrefix = "test"

      implicit val config: Config =
        Config.default.copy(outputFilePrefix = outputFilePrefix,
                            outputDirectory = tempDir.getRoot.getAbsolutePath,
                            resolution = 1,
                            isLocal = true)

      val rasterExtent = RasterExtent(
        Extent(-180, -90, 180, 90).expandBy(config.resolution),
        CellSize(config.resolution, config.resolution)
      )

      val rasterMatrix =
        IntArrayTile.fill(0, rasterExtent.cols, rasterExtent.rows)

      GeoTiffRepository.createOutputFiles((rasterExtent, rasterMatrix))

      val files = findGeneratedFiles(tempDir.getRoot.getAbsolutePath)

      softly
        .assertThat(files)
        .allMatch(file => file.startsWith(outputFilePrefix))
    }

}
