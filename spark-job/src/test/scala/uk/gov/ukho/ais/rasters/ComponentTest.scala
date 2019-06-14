package uk.gov.ukho.ais.rasters

import java.io.{File, IOException}
import java.nio.file.{Files, Path, StandardCopyOption}

import geotrellis.raster.io.geotiff.reader.GeoTiffReader
import org.apache.commons.io.FileUtils
import org.apache.spark.SparkException
import org.apache.spark.sql.SparkSession
import org.assertj.core.api.Assertions.assertThat
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
  private final val TOTAL_CELL_COUNT_WHOLE_WORLD_AT_1K = 64800
  @SuppressWarnings(
    Array("org.wartremover.warts.Var", "org.wartremover.warts.Null"))
  var testConfig: Config = _

  @SuppressWarnings(
    Array("org.wartremover.warts.Var", "org.wartremover.warts.Null"))
  var tempOutputDir: File = _

  @Before
  def beforeEach(): Unit = {
    val path: Path = Files.createTempDirectory("aisrastertest")
    tempOutputDir = path.toFile
    testConfig = Config("",
                        tempOutputDir.getAbsolutePath,
                        "test-prefix",
                        isLocal = true,
                        TEST_RESOLUTION,
                        6 * 60 * 60 * 1000,
                        30000)
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

    assertThat(sum).isEqualTo(0)
    assertThat(count).isEqualTo(TOTAL_CELL_COUNT_WHOLE_WORLD_AT_1K)

    assertTiffFileNameIsCorrect(geoTiff.tifFileName)
    assertPngBeenCreated()
  }

  @Test
  def whenARepresentativeSampleIsInputtedThenATifIsProduced(): Unit = {
    generateTiffForInputFile("ais_1M_approx_2_hours.txt.gz")
    val expectedNumberOfPings = 3345106

    val geoTiff: CreatedTif = getTiffFile

    val (sum, count) = geoTiff.calculateSumAndCount()

    assertThat(sum).isEqualTo(expectedNumberOfPings)
    assertThat(count).isEqualTo(TOTAL_CELL_COUNT_WHOLE_WORLD_AT_1K)

    assertTiffFileNameIsCorrect(geoTiff.tifFileName)
    assertPngBeenCreated()
  }

  @Test
  def whenAGappySampleIsInputtedWithDefaultTimeAndDistanceThresholdsThenATifIsProducedWithInterpolatedPoints()
    : Unit = {
    generateTiffForInputFile("resampling_test.txt")
    val expectedNumberOfPings = 6

    val geoTiff: CreatedTif = getTiffFile

    val (sum, count) = geoTiff.calculateSumAndCount()

    assertThat(sum).isEqualTo(expectedNumberOfPings)
    assertThat(count).isEqualTo(TOTAL_CELL_COUNT_WHOLE_WORLD_AT_1K)

    assertTiffFileNameIsCorrect(geoTiff.tifFileName)
    assertPngBeenCreated()
  }

  @Test
  def whenAGappySampleIsInputtedWithGivenTimeThresholdThenATifIsProducedWithInterpolatedPoints()
    : Unit = {
    testConfig = testConfig.copy(
      interpolationTimeThresholdMilliseconds = (6 * 60 - 1) * 1000)
    generateTiffForInputFile("resampling_test.txt")
    val expectedNumberOfPings = 4

    val geoTiff: CreatedTif = getTiffFile

    val (sum, count) = geoTiff.calculateSumAndCount()

    assertThat(sum).isEqualTo(expectedNumberOfPings)
    assertThat(count).isEqualTo(TOTAL_CELL_COUNT_WHOLE_WORLD_AT_1K)

    assertTiffFileNameIsCorrect(geoTiff.tifFileName)
    assertPngBeenCreated()
  }

  @Test
  def whenAGappySampleIsInputtedWithGivenDistanceThresholdThenATifIsProducedWithInterpolatedPoints()
    : Unit = {
    testConfig = testConfig.copy(interpolationDistanceThresholdMeters = 1000)
    generateTiffForInputFile("resampling_test_distance_threshold.txt")
    val expectedNumberOfPings = 5

    val geoTiff: CreatedTif = getTiffFile

    val (sum, count) = geoTiff.calculateSumAndCount()

    assertThat(sum).isEqualTo(expectedNumberOfPings)
    assertThat(count).isEqualTo(TOTAL_CELL_COUNT_WHOLE_WORLD_AT_1K)

    assertTiffFileNameIsCorrect(geoTiff.tifFileName)
    assertPngBeenCreated()
  }

  @Test
  def whenThePingsAreKnownThenTheCreatedTiffHasPingsInCorrectPlaces(): Unit = {
    generateTiffForInputFile("ais_6pings.txt")
    val expectedNumberOfPings = 6

    val geoTiff: CreatedTif = getTiffFile

    val (sum, count) = geoTiff.calculateSumAndCount()

    assertThat(sum).isEqualTo(expectedNumberOfPings)
    assertThat(count).isEqualTo(TOTAL_CELL_COUNT_WHOLE_WORLD_AT_1K)
    assertThat(geoTiff.tile.findMinMax).isEqualTo((0, 2))

    val sumPingsTopLeftCorner = geoTiff.getSumInRange(-180, 89.0, -179.0, 90.0)
    val sumPingsTopRightCorner = geoTiff.getSumInRange(178.9, 89.0, 180.0, 90.0)
    val sumPingsCentre = geoTiff.getSumInRange(-1.0, -1.0, 1.0, 1.0)
    val sumPingsBottomLeftCorner =
      geoTiff.getSumInRange(-180, -90.0, -179.0, -89.0)
    val sumPingsBottomRightCorner =
      geoTiff.getSumInRange(179.0, -90.0, 180.0, -89.0)

    assertThat(sumPingsTopLeftCorner).isEqualTo(1)
    assertThat(sumPingsTopRightCorner).isEqualTo(1)
    assertThat(sumPingsCentre).isEqualTo(1)
    assertThat(sumPingsBottomLeftCorner).isEqualTo(1)
    assertThat(sumPingsBottomRightCorner).isEqualTo(2)

    assertTiffFileNameIsCorrect(geoTiff.tifFileName)
    assertPngBeenCreated()
  }

  @Test
  def whenAisDataHasInvalidMessageTypesThenFiltersOutInvalidMessageTypes()
    : Unit = {
    generateTiffForInputFile("ais_mostly_invalid.txt")
    val expectedNumberOfValidMessages = 5

    val geoTiff: CreatedTif = getTiffFile

    val (sum, count) = geoTiff.calculateSumAndCount()

    assertThat(sum).isEqualTo(expectedNumberOfValidMessages)
    assertThat(count).isEqualTo(TOTAL_CELL_COUNT_WHOLE_WORLD_AT_1K)
    assertTiffFileNameIsCorrect(geoTiff.tifFileName)
    assertPngBeenCreated()
  }

  @Test(expected = classOf[SparkException])
  def whenFileIsANotDelimitedByTabsThenSparkExceptionThrown(): Unit = {
    generateTiffForInputFile("not_tsv.txt")
  }

  @Test(expected = classOf[SparkException])
  def whenCorruptZipFileIsIngestedThenSparkExceptionThrown(): Unit = {
    generateTiffForInputFile("corrupted_ais_1M_approx_2_hours.txt.gz")
  }

  private def assertPngBeenCreated(): Unit = {
    val pngFile = findGeneratedFile("png")
    assertThat(pngFile.exists()).isTrue

    assertFileNameCorrectAndHasExtension(pngFile.getName, "png")
  }

  def assertTiffFileNameIsCorrect(geoTiffFilename: String): Unit = {
    assertFileNameCorrectAndHasExtension(geoTiffFilename, "tif")
  }

  private def assertFileNameCorrectAndHasExtension(
      fileName: String,
      expectedExtension: String): Unit = {
    assertThat(fileName).startsWith(
      s"${testConfig.outputFilenamePrefix}-raster")
    assertThat(fileName).endsWith(s".$expectedExtension")
  }

  private def getTiffFile = {
    val geoTiffFile = findGeneratedFile("tif")
    new CreatedTif(GeoTiffReader.readSingleband(geoTiffFile.getAbsolutePath),
                   geoTiffFile.getName)
  }

  private def findGeneratedFile(fileExtension: String): File = {
    FileUtils
      .listFiles(tempOutputDir, Array(fileExtension), false)
      .stream()
      .findFirst()
      .get()
  }

  private def generateTiffForInputFile(fileOnClasspath: String): Unit = {
    val savedLocalFile = copyFileToFileSystem(fileOnClasspath)

    AisToRaster.generate(testConfig.copy(inputPath = savedLocalFile))
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
