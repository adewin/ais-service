package uk.gov.ukho.ais.rasters

import java.io.{File, IOException}
import java.nio.file.{Files, Path}

import geotrellis.raster.{CellSize, IntArrayTile, RasterExtent}
import geotrellis.vector.Extent
import org.apache.commons.io.FileUtils
import org.assertj.core.api.Assertions.assertThat
import org.junit.{After, Before, Test}

class FileCreatorTest {
  var tempOutputDir: File = _

  @Before
  def beforeEach(): Unit = {
    val path: Path = Files.createTempDirectory("aisrastertest")
    tempOutputDir = path.toFile
    ConfigParser.parse(
      Array(
        "-i",
        "",
        "-o",
        tempOutputDir.getAbsolutePath,
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
  }

  @After
  def afterEach(): Unit = {
    try {
      FileUtils.cleanDirectory(tempOutputDir)
      FileUtils.deleteDirectory(tempOutputDir)
    } catch {
      case e: IOException =>
        System.err.println(
          s"Unable to delete: $tempOutputDir due to '${e.getMessage}'")
    }
  }

  @Test
  def whenCalledWithRasterExtentAndMatrixThenFilesAreCreated(): Unit = {
    val resolution = ConfigParser.config.resolution

    val rasterExtent = RasterExtent(
      Extent(-180, -90, 180, 90).expandBy(resolution),
      CellSize(resolution, resolution)
    )

    val rasterMatrix =
      IntArrayTile.fill(0, rasterExtent.cols, rasterExtent.rows)

    val raster = (rasterExtent, rasterMatrix)

    FileCreator.createOutputFiles(raster)

    val png = findGeneratedFile("png")
    val tiff = findGeneratedFile("tif")

    assertThat(png.exists()).isTrue
    assertThat(tiff.exists()).isTrue

    assertThat(png.getName.indexOf("test")).isEqualTo(0)
    assertThat(tiff.getName.indexOf("test")).isEqualTo(0)
  }

  private def findGeneratedFile(fileExtension: String): File = {
    FileUtils
      .listFiles(tempOutputDir, Array(fileExtension), false)
      .stream()
      .findFirst()
      .get()
  }
}
