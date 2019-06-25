package uk.gov.ukho.ais.rasters

import java.io.{File, IOException}
import java.nio.file.{Files, Path}
import java.sql.Timestamp
import java.time.format.DateTimeFormatter
import java.time.{Instant, ZoneId}

import geotrellis.raster.io.geotiff.reader.GeoTiffReader
import org.apache.commons.io.FileUtils
import org.apache.spark.SparkException
import org.assertj.core.api.Assertions.assertThat
import org.junit.{After, Before, Test}

import scala.collection.mutable

class ComponentTest {

  Session.init(true)

  private final val TEST_RESOLUTION = 1
  private final val TOTAL_CELL_COUNT_WHOLE_WORLD_AT_1K = 65884

  var testConfig: TestingConfigBuilder = _

  var tempOutputDir: File = _

  private final val STATIC_DATA_FILE: String =
    ResourceService.copyFileToFileSystem("test_static_data.txt")
  private final val DRAUGHT_VESSEL_RANGE_FILE: String =
    ResourceService.copyFileToFileSystem("draught_data.txt")
  private final val DEFAULT_START_DATE = "1970-01-01"
  private final val DEFAULT_END_DATE = "3000-01-01"
  private final val DEFAULT_INTERPOLATION_METERS = "30000"
  private final val DEFAULT_INTERPOLATION_TIME =
    String.valueOf(6 * 60 * 60 * 1000)

  @Before
  def beforeEach(): Unit = {
    val path: Path = Files.createTempDirectory("aisrastertest")
    tempOutputDir = path.toFile
    testConfig = TestingConfigBuilder
      .fromDefaults()
      .outputDirectory(tempOutputDir.getAbsolutePath)
      .staticDataFile(STATIC_DATA_FILE)
      .startPeriod(DEFAULT_START_DATE)
      .endPeriod(DEFAULT_END_DATE)
      .interpolationDistanceThresholdMeters(DEFAULT_INTERPOLATION_METERS)
      .interpolationTimeThresholdMilliseconds(DEFAULT_INTERPOLATION_TIME)
      .draughtConfigFile(DRAUGHT_VESSEL_RANGE_FILE)
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
    testConfig = testConfig.interpolationTimeThresholdMilliseconds(
      s"${(6L * 60L - 1L) * 1000L}")

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
    testConfig = testConfig.interpolationDistanceThresholdMeters("1000")
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

  @Test
  def whenDateFilterAppliedThenFiltersOutPingsOutsideThatRange(): Unit = {

    testConfig = testConfig.startPeriod("2019-01-01").endPeriod("2019-02-01")

    generateTiffForInputFile("ais_6pings_known_time.txt")
    val expectedNumberOfValidMessages = 1

    val geoTiff: CreatedTif = getTiffFile

    val (sum, count) = geoTiff.calculateSumAndCount()

    assertThat(sum).isEqualTo(expectedNumberOfValidMessages)
    assertThat(count).isEqualTo(TOTAL_CELL_COUNT_WHOLE_WORLD_AT_1K)
  }

  @Test
  def whenDateFilterNotAppliedThenAllPingsIncluded(): Unit = {
    generateTiffForInputFile("ais_6pings_known_time.txt")
    val expectedNumberOfValidMessages = 6

    val geoTiff: CreatedTif = getTiffFile

    val (sum, count) = geoTiff.calculateSumAndCount()

    assertThat(sum).isEqualTo(expectedNumberOfValidMessages)
    assertThat(count).isEqualTo(TOTAL_CELL_COUNT_WHOLE_WORLD_AT_1K)

  }

  @Test
  def whenVesselDraughtFilterAppliedThenPingsNotInRangeFilteredOut(): Unit = {
    testConfig = testConfig.draughtIndex("1")

    generateTiffForInputFile("ais_6pings.txt")
    val expectedNumberOfValidMessages = 1

    val geoTiff: CreatedTif = getTiffFile

    val (sum, count) = geoTiff.calculateSumAndCount()

    assertThat(sum).isEqualTo(expectedNumberOfValidMessages)
    assertThat(count).isEqualTo(TOTAL_CELL_COUNT_WHOLE_WORLD_AT_1K)
  }

  @Test
  def whenFilteringUnknownVesselDraughtThenVesselsWithKnownDraughtFilteredOut()
    : Unit = {
    testConfig = testConfig.draughtIndex("unknown")

    generateTiffForInputFile("ais_6pings.txt")
    val expectedNumberOfValidMessages = 4

    val geoTiff: CreatedTif = getTiffFile

    val (sum, count) = geoTiff.calculateSumAndCount()

    assertThat(sum).isEqualTo(expectedNumberOfValidMessages)
    assertThat(count).isEqualTo(TOTAL_CELL_COUNT_WHOLE_WORLD_AT_1K)
  }

  @Test(expected = classOf[IllegalStateException])
  def whenNoParametersPassedToSparkJobThenIllegalStateExceptionIsRaised()
    : Unit = {
    AisToRaster.main(Array())
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
    val savedLocalFile = ResourceService.copyFileToFileSystem(fileOnClasspath)

    testConfig = testConfig.inputPath(savedLocalFile)

    AisToRaster.main(testConfig.build())
  }

}
