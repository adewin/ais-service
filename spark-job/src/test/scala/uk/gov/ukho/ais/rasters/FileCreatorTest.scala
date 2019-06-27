package uk.gov.ukho.ais.rasters

import java.io.{File, IOException}
import java.nio.file.{Files, Path}

import geotrellis.raster.{CellSize, IntArrayTile, RasterExtent}
import geotrellis.vector.Extent
import org.apache.commons.io.FileUtils
import org.assertj.core.api.Assertions.assertThat
import org.junit.{After, AfterClass, Before, Test}

class FileCreatorTest {
  private final val TEMP_OUTPUT_DIRECTORY: File =
    Files.createTempDirectory("aisrastertest").toFile
  private final val TEST_CONFIG: Config = ConfigParser.parse(
    Array(
      "-i",
      "",
      "-o",
      TEMP_OUTPUT_DIRECTORY.getAbsolutePath,
      "-p",
      "test",
      "-r",
      "1",
      "-t",
      "3",
      "-d",
      "3",
      "-s",
      "1970-01-01",
      "-e",
      "1970-01-01",
      "--draughtConfigFile",
      "",
      "--staticDataFile",
      "",
      "-l"
    ))

  {
    TEMP_OUTPUT_DIRECTORY.deleteOnExit()
  }

  @After
  def afterEach(): Unit = {
    try {
      FileUtils.cleanDirectory(TEMP_OUTPUT_DIRECTORY)
    } catch {
      case e: IOException =>
        System.err.println(
          s"Unable to delete: $TEMP_OUTPUT_DIRECTORY due to '${e.getMessage}'")
    }
  }

  @Test
  def whenCalledWithRasterExtentAndMatrixThenFilesAreCreated(): Unit = {
    val resolution = TEST_CONFIG.resolution

    val rasterExtent = RasterExtent(
      Extent(-180, -90, 180, 90).expandBy(resolution),
      CellSize(resolution, resolution)
    )

    val rasterMatrix =
      IntArrayTile.fill(0, rasterExtent.cols, rasterExtent.rows)

    val raster = (rasterExtent, rasterMatrix)

    FileCreator.createOutputFiles(raster, TEST_CONFIG)

    val png = findGeneratedFile("png")
    val tiff = findGeneratedFile("tif")

    assertThat(png.exists()).isTrue
    assertThat(tiff.exists()).isTrue

    assertThat(png.getName.indexOf("test")).isEqualTo(0)
    assertThat(tiff.getName.indexOf("test")).isEqualTo(0)
  }

  private def findGeneratedFile(fileExtension: String): File = {
    FileUtils
      .listFiles(TEMP_OUTPUT_DIRECTORY, Array(fileExtension), false)
      .stream()
      .findFirst()
      .get()
  }
}
