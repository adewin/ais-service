package uk.gov.ukho.ais.heatmaps.generator

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}
import java.sql.{Connection, PreparedStatement, ResultSet, Timestamp}
import java.time.{LocalDateTime, ZoneOffset}

import com.amazonaws.services.s3.AmazonS3
import geotrellis.raster.io.geotiff.reader.GeoTiffReader
import javax.sql.DataSource
import org.apache.commons.io.{FileUtils, FilenameUtils}
import org.assertj.core.api.SoftAssertions
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.junit.{Before, Rule, Test}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito._
import org.mockito.junit.MockitoJUnitRunner
import uk.gov.ukho.ais.heatmaps.generator.FileUtilities.findGeneratedFiles
import uk.gov.ukho.ais.heatmaps.generator.utility.TimeUtilities.makeTimestamp

@RunWith(classOf[MockitoJUnitRunner])
class ComponentTest {

  val _tempDir = new TemporaryFolder()

  @Rule
  def tempDir: TemporaryFolder = _tempDir

  val datasourceMock: DataSource = mock(classOf[DataSource])
  val connectionMock: Connection = mock(classOf[Connection])
  val preparedStatementMock: PreparedStatement = mock(
    classOf[PreparedStatement])
  val resultSetMock: ResultSet = mock(classOf[ResultSet])

  val filterQuery = "SELECT * FROM table"

  private final val TOTAL_CELL_COUNT_WHOLE_WORLD_AT_1K = 65884
  private final val DEFAULT_INTERPOLATION_METERS = 30000
  private final val DEFAULT_INTERPOLATION_TIME = 6 * 60 * 60 * 1000

  private var testConfig: Config = _
  private implicit val mockAmazonS3: AmazonS3 = mock(classOf[AmazonS3])

  @Before
  def setup(): Unit = {
    when(datasourceMock.getConnection()).thenReturn(connectionMock)
    when(connectionMock.prepareStatement(anyString()))
      .thenReturn(preparedStatementMock)
    when(preparedStatementMock.executeQuery()).thenReturn(resultSetMock)

    testConfig = Config.default.copy(
      outputDirectory = tempDir.getRoot.getAbsolutePath,
      filterSqlFile = createFilterSqlFile.getAbsolutePath,
      resolution = 1,
      isLocal = true,
      month = 1,
      year = 1970)
  }

  @Test
  def whenGivenAnEmptySetOfPingsThenHeatmapIsGenerated(): Unit =
    SoftAssertions.assertSoftly { softly =>
      implicit val config: Config = testConfig

      setDataReturnedFromDataSource()

      HeatmapOrchestrator.orchestrateHeatmapGeneration(datasourceMock)

      val files =
        findGeneratedFiles(tempDir.getRoot.getAbsolutePath).map(file =>
          FilenameUtils.getExtension(file))

      softly.assertThat(files).containsExactly("tif")
    }

  @Test
  def whenGivenASetOfPingsThenHeatmapHasPingsInCorrectPlaces(): Unit =
    SoftAssertions.assertSoftly { softly =>
      implicit val config: Config = testConfig

      setDataReturnedFromDataSource(
        ("123", 10, 179.9, -89.9),
        ("123", 20, -179.9, 89.9),
        ("123", 30, 0, 0),
        ("123", 40, 179.9, 89.9),
        ("123", 50, -179.9, -89.9),
        ("123", 60, 179.9, -89.9)
      )

      HeatmapOrchestrator.orchestrateHeatmapGeneration(datasourceMock)

      val filePath = findGeneratedFiles(tempDir.getRoot.getAbsolutePath)
        .find(file => FilenameUtils.getExtension(file) == "tif")
        .map(filename =>
          Paths.get(tempDir.getRoot.getAbsolutePath, filename).toString)

      filePath match {
        case Some(filePath) =>
          val geoTiff = openTiffFile(filePath)

          val (sum, count) = geoTiff.calculateSumAndCount()

          val expectedNumberOfPings = 6

          softly.assertThat(sum).isEqualTo(expectedNumberOfPings)
          softly.assertThat(count).isEqualTo(TOTAL_CELL_COUNT_WHOLE_WORLD_AT_1K)

          val (min, max) = geoTiff.tile.findMinMax
          softly.assertThat(min).isEqualTo(0)
          softly.assertThat(max).isEqualTo(2)

          val sumPingsTopLeftCorner =
            geoTiff.getSumInRange(-180, 89.0, -179.0, 90.0)
          val sumPingsTopRightCorner =
            geoTiff.getSumInRange(178.9, 89.0, 180.0, 90.0)
          val sumPingsCentre = geoTiff.getSumInRange(-1.0, -1.0, 1.0, 1.0)
          val sumPingsBottomLeftCorner =
            geoTiff.getSumInRange(-180, -90.0, -179.0, -89.0)
          val sumPingsBottomRightCorner =
            geoTiff.getSumInRange(179.0, -90.0, 180.0, -89.0)

          softly.assertThat(sumPingsTopLeftCorner).isEqualTo(1)
          softly.assertThat(sumPingsTopRightCorner).isEqualTo(1)
          softly.assertThat(sumPingsCentre).isEqualTo(1)
          softly.assertThat(sumPingsBottomLeftCorner).isEqualTo(1)
          softly.assertThat(sumPingsBottomRightCorner).isEqualTo(2)
        case None => softly.fail("tif file not found")
      }
    }

  @Test
  def whenGivenASetOfPingsWithLastPingsWithin3MinTimeStepThenPingsCorrectlyInterpolated()
    : Unit =
    SoftAssertions.assertSoftly { softly =>
      implicit val config: Config = testConfig.copy(
        interpolationTimeThresholdMilliseconds = DEFAULT_INTERPOLATION_TIME,
        interpolationDistanceThresholdMeters = DEFAULT_INTERPOLATION_METERS)

      setDataReturnedFromDataSource(
        ("123456793", 0, -1.216151956, 50.77512703),
        ("123456793", 60 * 6, -1.198185651, 50.78648692),
        ("123456793", 60 * 12, -1.180219345, 50.77512703),
        ("123456795", 60 * 9, -1.180219345, 50.76944604),
        ("123456795", 60 * 20, -1.180219345, 50.76944604),
        ("123456795", 60 * 21, -1.180218345, 50.76944604)
      )

      HeatmapOrchestrator.orchestrateHeatmapGeneration(datasourceMock)

      val filePath = findGeneratedFiles(tempDir.getRoot.getAbsolutePath)
        .find(file => FilenameUtils.getExtension(file) == "tif")
        .map(filename =>
          Paths.get(tempDir.getRoot.getAbsolutePath, filename).toString)

      filePath match {
        case Some(filePath) =>
          val geoTiff = openTiffFile(filePath)

          val (sum, count) = geoTiff.calculateSumAndCount()

          softly.assertThat(sum).isEqualTo(10)
          softly.assertThat(count).isEqualTo(TOTAL_CELL_COUNT_WHOLE_WORLD_AT_1K)
      }
    }

  @Test
  def whenGivenASetOfPingsWithPingsOutsideOfMonthThenPingsAreFiltered(): Unit =
    SoftAssertions.assertSoftly { softly =>
      implicit val config: Config = testConfig.copy(
        interpolationTimeThresholdMilliseconds = DEFAULT_INTERPOLATION_TIME,
        interpolationDistanceThresholdMeters = DEFAULT_INTERPOLATION_METERS,
        year = 2018)

      val baseDateTime = LocalDateTime.of(2018, 1, 1, 0, 0, 0)

      setDataReturnedFromDataSource(
        ("123456793",
         baseDateTime.minusMinutes(24).toEpochSecond(ZoneOffset.UTC),
         -1.216151956,
         50.77512703),
        ("123456793",
         baseDateTime.plusMinutes(6).toEpochSecond(ZoneOffset.UTC),
         -1.198185651,
         50.78648692),
        ("123456793",
         baseDateTime.plusMinutes(12).toEpochSecond(ZoneOffset.UTC),
         -1.180219345,
         50.77512703)
      )

      HeatmapOrchestrator.orchestrateHeatmapGeneration(datasourceMock)

      val filePath = findGeneratedFiles(tempDir.getRoot.getAbsolutePath)
        .find(file => FilenameUtils.getExtension(file) == "tif")
        .map(filename =>
          Paths.get(tempDir.getRoot.getAbsolutePath, filename).toString)

      filePath match {
        case Some(filePath) =>
          val geoTiff = openTiffFile(filePath)

          val (sum, count) = geoTiff.calculateSumAndCount()

          val expectedNumberOfPings = 5

          softly.assertThat(sum).isEqualTo(expectedNumberOfPings)
          softly.assertThat(count).isEqualTo(TOTAL_CELL_COUNT_WHOLE_WORLD_AT_1K)
      }
    }

  @Test
  def whenHeatmapGeneratedThenSqlFilterIsUsed(): Unit =
    SoftAssertions.assertSoftly { softly =>
      implicit val config: Config = testConfig

      setDataReturnedFromDataSource(
        ("123", 10, 179.9, -89.9),
        ("456", 20, -179.9, 89.9),
        ("789", 30, 0, 0),
        ("234", 40, 179.9, 89.9),
        ("567", 50, -179.9, -89.9),
        ("890", 60, 179.9, -89.9)
      )

      HeatmapOrchestrator.orchestrateHeatmapGeneration(datasourceMock)

      val preparedStatementArgCaptor: ArgumentCaptor[String] =
        ArgumentCaptor.forClass(classOf[String])

      verify(connectionMock, times(31))
        .prepareStatement(preparedStatementArgCaptor.capture())

      softly
        .assertThat(preparedStatementArgCaptor.getAllValues.iterator())
        .allMatch { sqlStatement =>
          sqlStatement.startsWith(s"""
            |SELECT mmsi, acquisition_time, lat, lon
            |FROM ($filterQuery)
            |WHERE (
            |(year = ${config.year} AND month = ${config.month})
            |""".stripMargin)
        }

    }

  private def openTiffFile(filename: String) =
    new CreatedTiff(GeoTiffReader.readSingleband(filename))

  private def setDataReturnedFromDataSource(
      data: (String, Long, Double, Double)*): Unit = {
    data.toList.map {
      case (mmsi: String, timeSeconds: Long, lon: Double, lat: Double) =>
        List(mmsi, makeTimestamp(timeSeconds), lon, lat, true)
    }.transpose match {
      case List(
          mmsi: List[String],
          timeSeconds: List[Timestamp],
          lon: List[Double],
          lat: List[Double],
          hasNext: List[Boolean]
          ) =>
        when(resultSetMock.getString("mmsi"))
          .thenReturn(mmsi.head, mmsi.tail: _*)
        when(resultSetMock.getTimestamp("acquisition_time"))
          .thenReturn(timeSeconds.head, timeSeconds.tail: _*)
        when(resultSetMock.getDouble("lon")).thenReturn(lon.head, lon.tail: _*)
        when(resultSetMock.getDouble("lat")).thenReturn(lat.head, lat.tail: _*)
        when(resultSetMock.next())
          .thenReturn(hasNext.head, hasNext.tail: _*)
          .thenReturn(false)
      case _ => when(resultSetMock.next()).thenReturn(false)
    }
  }

  private def createFilterSqlFile = {
    val tmpSqlFile = Files.createTempFile("unfiltered", ".sql").toFile
    FileUtils.writeStringToFile(tmpSqlFile,
                                filterQuery,
                                StandardCharsets.UTF_8.toString)
    tmpSqlFile
  }
}
