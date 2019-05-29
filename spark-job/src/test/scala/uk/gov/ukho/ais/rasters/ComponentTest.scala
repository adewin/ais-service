package uk.gov.ukho.ais.rasters

import java.io.{File, IOException}
import java.nio.file.{Files, Path, StandardCopyOption}

import geotrellis.raster.io.geotiff.reader.GeoTiffReader
import org.apache.commons.io.FileUtils
import org.apache.spark.SparkException
import org.apache.spark.sql.SparkSession
import org.junit.{After, Before, Test}

class ComponentTest {

  val spark: SparkSession = {
    SparkSession
      .builder()
      .master("local")
      .appName("Spark Testing")
      .getOrCreate()
  }

  private final val TEST_RESOLUTION = 1
  private final val TOTAL_CELL_COUNT = 64800

  @SuppressWarnings(
    Array("org.wartremover.warts.Var", "org.wartremover.warts.Null"))
  var tempOutputDir: File = _

  @Before
  def beforeEach(): Unit = {
    val path: Path = Files.createTempDirectory("aisrastertest")
    tempOutputDir = path.toFile
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
  def whenEmptyInputThenEmptyTiffReturned(): Unit = {
    generateTiffForInputFile("empty.txt")

    val geoTiff: CreatedTif = getTiffFile

    val (sum, count) = geoTiff.calculateSumAndCount()

    assert(sum == 0)
    assert(count == TOTAL_CELL_COUNT)
    assertPngBeenCreated()
  }

  @Test
  def whenARepresentativeSampleIsInputtedThenATifIsProduced(): Unit = {
    generateTiffForInputFile("ais_1M_approx_2_hours.txt.gz")
    val expectedNumberOfPings = 991266

    val geoTiff: CreatedTif = getTiffFile

    val (sum, count) = geoTiff.calculateSumAndCount()

    assert(sum == expectedNumberOfPings)
    assert(count == TOTAL_CELL_COUNT)
    assertPngBeenCreated()
  }

  @Test
  def whenThePingsAreKnownThenTheCreatedTiffHasPingsInCorrectPlaces(): Unit = {
    generateTiffForInputFile("ais_6pings.txt")
    val expectedNumberOfPings = 6

    val geoTiff: CreatedTif = getTiffFile

    val (sum, count) = geoTiff.calculateSumAndCount()

    assert(sum == expectedNumberOfPings)
    assert(count == TOTAL_CELL_COUNT)
    assert(geoTiff.tile.findMinMax == (0, 2))

    val sumPingsTopLeftCorner = geoTiff.getSumInRange(-180, 89.0, -179.0, 90.0)
    val sumPingsTopRightCorner = geoTiff.getSumInRange(178.9, 89.0, 180.0, 90.0)
    val sumPingsCentre = geoTiff.getSumInRange(-1.0, -1.0, 1.0, 1.0)
    val sumPingsBottomLeftCorner =
      geoTiff.getSumInRange(-180, -90.0, -179.0, -89.0)
    val sumPingsBottomRightCorner =
      geoTiff.getSumInRange(179.0, -90.0, 180.0, -89.0)

    assert(sumPingsTopLeftCorner == 1)
    assert(sumPingsTopRightCorner == 1)
    assert(sumPingsCentre == 1)
    assert(sumPingsBottomLeftCorner == 1)
    assert(sumPingsBottomRightCorner == 2)

    assertPngBeenCreated()
  }

  @Test
  def whenAisDataHasInvalidMessageTypesThenFiltersOutInvalidMessageTypes()
    : Unit = {
    generateTiffForInputFile("ais_mostly_invalid.txt")
    val expectedNumberOfValidMessages = 5

    val geoTiff: CreatedTif = getTiffFile

    val (sum, count) = geoTiff.calculateSumAndCount()

    assert(sum == expectedNumberOfValidMessages)
    assert(count == TOTAL_CELL_COUNT)
    assertPngBeenCreated()
  }

  @Test
  def whenFileIsANotDelimitedByTabsThenSparkExceptionThrown(): Unit = {
    try {
      generateTiffForInputFile("not_tsv.txt")
    } catch {
      case sparkException: SparkException =>
        System.err.println(sparkException.getMessage)
      case otherException: Exception =>
        System.err.println(
          s"Not of type: SparkException: ${otherException.getClass.getCanonicalName}, " +
            s"message: ${otherException.getMessage}")
        assert(false)
    }
  }

  @Test
  def whenCorruptZipFileIsIngestedThenSparkExceptionThrown(): Unit = {
    try {
      generateTiffForInputFile("corrupted_ais_1M_approx_2_hours.txt.gz")
    } catch {
      case sparkException: SparkException =>
        System.err.println(sparkException.getMessage)
        assert(sparkException.getCause.getClass == classOf[IOException])
      case otherException: Exception =>
        System.err.println(
          s"Not of type: SparkException: ${otherException.getClass.getCanonicalName}, " +
            s"message: ${otherException.getMessage}")
        assert(false)
    }
  }

  private def assertPngBeenCreated(): Unit = {
    val pngFile = findGeneratedFile("png")
    assert(pngFile.exists())
  }

  private def getTiffFile = {
    val geoTiffFilename = findGeneratedFile("tif")
    new CreatedTif(
      GeoTiffReader.readSingleband(geoTiffFilename.getAbsolutePath))
  }

  private def findGeneratedFile(fileExtension: String) = {
    FileUtils
      .listFiles(tempOutputDir, Array(fileExtension), false)
      .stream()
      .findFirst()
      .orElseThrow(() => {
        new IllegalArgumentException()
      })
  }

  private def generateTiffForInputFile(filename: String): Unit = {
    val notEmptyFile = copyFileToFileSystem(filename)
    val testConfig: Config =
      Config(notEmptyFile, tempOutputDir.getAbsolutePath, true, TEST_RESOLUTION)
    AisToRaster.generate(testConfig)
  }

  @SuppressWarnings(Array("org.wartremover.warts.Throw"))
  def copyFileToFileSystem(filename: String): String = {
    val resourceInputStream = getClass.getResourceAsStream(s"/$filename")
    val tempFile: File = File.createTempFile("scalatest", filename)

    if (resourceInputStream == null)
      throw new IllegalArgumentException(
        s"File: $filename does not exist or cannot be read")

    Files.copy(resourceInputStream,
               tempFile.toPath,
               StandardCopyOption.REPLACE_EXISTING)

    tempFile.deleteOnExit()
    tempFile.getAbsolutePath
  }
}
