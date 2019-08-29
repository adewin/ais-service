package uk.gov.ukho.ais.heatmaps.repository

import geotrellis.raster.{CellSize, IntArrayTile, RasterExtent}
import geotrellis.vector.Extent
import uk.gov.ukho.ais.heatmaps.Config
import uk.gov.ukho.ais.heatmaps.FileUtilities.findGeneratedFiles
import org.apache.commons.io.FilenameUtils
import org.assertj.core.api.SoftAssertions
import org.junit.rules.TemporaryFolder
import org.junit.{Rule, Test}

class GeoTiffRepositoryTest {

  val _tempDir = new TemporaryFolder()

  @Rule
  def tempDir: TemporaryFolder = _tempDir

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

      softly.assertThat(files).containsExactlyInAnyOrder("png", "tif")
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
