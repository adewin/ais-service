package uk.gov.ukho.ais.resampler

import java.io.{File, FileInputStream}
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}
import java.sql.{Connection, PreparedStatement, ResultSet, Timestamp}
import java.time.{LocalDateTime, ZoneOffset}

import com.amazonaws.services.s3.AmazonS3
import javax.sql.DataSource
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream
import org.apache.commons.io.{FileUtils, FilenameUtils, IOUtils}
import org.assertj.core.api.SoftAssertions
import org.junit.rules.TemporaryFolder
import org.junit.{Before, Rule, Test}
import org.mockito.ArgumentMatchers._
import org.mockito.IdiomaticMockito
import org.mockito.Mockito._
import org.mockito.captor.ArgCaptor
import uk.gov.ukho.ais.resampler.FileUtilities.findGeneratedFiles
import uk.gov.ukho.ais.resampler.utility.TimeUtilities.makeTimestamp

import scala.collection.JavaConverters._

class ComponentTest extends IdiomaticMockito {

  val _tempDir = new TemporaryFolder()

  @Rule
  def tempDir: TemporaryFolder = _tempDir

  implicit val dataSourceMock: DataSource = mock[DataSource]
  val connectionMock: Connection = mock[Connection]
  val preparedStatementMock: PreparedStatement = mock[PreparedStatement]
  val resultSetMock: ResultSet = mock[ResultSet]
  val pairResultSetMock: ResultSet = mock[ResultSet]

  val filterQuery = "SELECT * FROM table"

  private final val BZ2_EXTENSION: String = "bz2"
  private final val DEFAULT_INTERPOLATION_METERS = 30000
  private final val DEFAULT_INTERPOLATION_TIME = 6 * 60 * 60 * 1000

  private var testConfig: Config = _
  private implicit val mockAmazonS3: AmazonS3 = mock[AmazonS3]

  val baseYear = 2019
  val baseMonth = 1
  val baseDateTime: LocalDateTime =
    LocalDateTime.of(baseYear, baseMonth, 1, 0, 0, 0)

  @Before
  def setup(): Unit = {
    dataSourceMock.getConnection() returns connectionMock
    connectionMock.prepareStatement(any[String]) returns preparedStatementMock
    preparedStatementMock
      .executeQuery() returns pairResultSetMock andThen resultSetMock

    setYearAndMonthPairsReturnedFromDataSource(
      (baseYear, baseMonth)
    )

    testConfig = Config.default.copy(
      outputDirectory = tempDir.getRoot.getAbsolutePath,
      database = "database",
      table = "table",
      inputFiles = Seq("s3://test/input-ais.tar.bz2"),
      resolution = 1,
      isLocal = true
    )
  }

  @Test
  def whenGivenAnEmptySetOfPingsThenNoCsvIsGenerated(): Unit =
    SoftAssertions.assertSoftly { softly =>
      implicit val config: Config = testConfig

      setDataReturnedFromDataSource(Seq.empty)

      ResamplerOrchestrator.orchestrateResampling()

      val filePath = findGeneratedFiles(tempDir.getRoot.getAbsolutePath)
        .find(file => FilenameUtils.getExtension(file) == BZ2_EXTENSION)
        .map(filename =>
          Paths.get(tempDir.getRoot.getAbsolutePath, filename).toString)

      filePath match {
        case Some(filePath) => softly.fail(s"csv should not exist: $filePath")
        case None           =>
      }
    }

  @Test
  def whenGivenASetOfPingsThenCsvHasPingsInCorrectPlaces(): Unit =
    SoftAssertions.assertSoftly { softly =>
      implicit val config: Config = testConfig

      val expectedPings = Seq(
        ("123",
         baseDateTime.plusSeconds(10).toEpochSecond(ZoneOffset.UTC),
         179.9,
         -89.9),
        ("456",
         baseDateTime.plusSeconds(20).toEpochSecond(ZoneOffset.UTC),
         -179.9,
         89.9),
        ("789",
         baseDateTime.plusSeconds(30).toEpochSecond(ZoneOffset.UTC),
         0d,
         0d),
        ("234",
         baseDateTime.plusSeconds(40).toEpochSecond(ZoneOffset.UTC),
         179.9,
         89.9),
        ("567",
         baseDateTime.plusSeconds(50).toEpochSecond(ZoneOffset.UTC),
         -179.9,
         -89.9),
        ("890",
         baseDateTime.plusSeconds(60).toEpochSecond(ZoneOffset.UTC),
         179.9,
         -89.9)
      )

      setDataReturnedFromDataSource(expectedPings)

      ResamplerOrchestrator.orchestrateResampling()

      val filePath = findGeneratedFiles(tempDir.getRoot.getAbsolutePath)
        .find(file => FilenameUtils.getExtension(file) == BZ2_EXTENSION)
        .map(filename =>
          Paths.get(tempDir.getRoot.getAbsolutePath, filename).toString)

      filePath match {
        case Some(filePath) =>
          val csv = openCsvFile(filePath)

          softly.assertThat(csv.size).isEqualTo(6)
          softly
            .assertThat(csv.toArray)
            .containsExactlyElementsOf(expectedPings.asJava)

        case None => softly.fail("csv file not found")
      }
    }

  @Test
  def whenGivenASetOfPingsThenPingsCorrectlyInterpolated(): Unit =
    SoftAssertions.assertSoftly { softly =>
      implicit val config: Config = testConfig.copy(
        interpolationTimeThresholdMilliseconds = DEFAULT_INTERPOLATION_TIME,
        interpolationDistanceThresholdMeters = DEFAULT_INTERPOLATION_METERS)

      val pings = Seq(
        ("123456793",
         baseDateTime.toEpochSecond(ZoneOffset.UTC),
         -1.216151956,
         50.77512703),
        ("123456793",
         baseDateTime.plusMinutes(6).toEpochSecond(ZoneOffset.UTC),
         -1.198185651,
         50.78648692),
        ("123456793",
         baseDateTime.plusMinutes(12).toEpochSecond(ZoneOffset.UTC),
         -1.180219345,
         50.77512703),
        ("123456793",
         baseDateTime.plusMinutes(13).toEpochSecond(ZoneOffset.UTC),
         -1.180202123,
         50.77235519),
        ("123456793",
         baseDateTime.plusMinutes(15).toEpochSecond(ZoneOffset.UTC),
         -1.180219345,
         50.76944604)
      )

      setDataReturnedFromDataSource(pings)

      ResamplerOrchestrator.orchestrateResampling()

      val filePaths = findGeneratedFiles(tempDir.getRoot.getAbsolutePath)
        .filter(file => FilenameUtils.getExtension(file) == BZ2_EXTENSION)
        .map(filename =>
          Paths.get(tempDir.getRoot.getAbsolutePath, filename).toString)

      softly.assertThat(filePaths).hasSize(1)

      filePaths.headOption match {
        case Some(filePath) =>
          val csv = openCsvFile(filePath)

          softly.assertThat(csv.size).isEqualTo(6)

        case None => softly.fail("csv file not found")
      }
    }

  @Test
  def whenGivenASetOfPingsWithPingsOutsideOfMonthThenPingsAreFiltered(): Unit =
    SoftAssertions.assertSoftly { softly =>
      implicit val config: Config = testConfig.copy(
        interpolationTimeThresholdMilliseconds = DEFAULT_INTERPOLATION_TIME,
        interpolationDistanceThresholdMeters = DEFAULT_INTERPOLATION_METERS)

      setDataReturnedFromDataSource(
        Seq(
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
        ))

      ResamplerOrchestrator.orchestrateResampling()

      val filePath = findGeneratedFiles(tempDir.getRoot.getAbsolutePath)
        .find(file => FilenameUtils.getExtension(file) == BZ2_EXTENSION)
        .map(filename =>
          Paths.get(tempDir.getRoot.getAbsolutePath, filename).toString)

      filePath match {
        case Some(filePath) =>
          val csv = openCsvFile(filePath)

          val expectedNumberOfPings = 5

          softly.assertThat(csv.size).isEqualTo(expectedNumberOfPings)

        case None => softly.fail("csv file not found")
      }
    }

  @Test
  def whenCsvGeneratedThenSqlIsUsed(): Unit =
    SoftAssertions.assertSoftly { softly =>
      implicit val config: Config = testConfig

      setDataReturnedFromDataSource(
        Seq(
          ("123", 10, 179.9, -89.9),
          ("456", 20, -179.9, 89.9),
          ("789", 30, 0, 0),
          ("234", 40, 179.9, 89.9),
          ("567", 50, -179.9, -89.9),
          ("890", 60, 179.9, -89.9)
        ))

      ResamplerOrchestrator.orchestrateResampling()

      val captor = ArgCaptor[String]
      connectionMock.prepareStatement(captor) wasCalled 32.times

      softly
        .assertThat(captor.values.head)
        .startsWith(s"""
             |SELECT DISTINCT year, month FROM ?
             |WHERE input_ais_data_file in (?)
             |""".stripMargin)

      softly
        .assertThat(captor.values.tail.iterator.asJava)
        .allMatch(
          _.startsWith(s"""
               |SELECT *
               |FROM ?
               |WHERE (
               |""".stripMargin)
        )
    }

  private def openCsvFile(
      filename: String): Seq[(String, Long, Double, Double)] = {
    val lines: java.util.List[_] = IOUtils.readLines(
      new BZip2CompressorInputStream(new FileInputStream(new File(filename))))

    lines.asScala
      .map(_.toString)
      .map(_.split("\t") match {
        case Array(_, mmsi, timestamp, lon, lat, _*) =>
          (mmsi,
           Timestamp
             .valueOf(timestamp)
             .toLocalDateTime
             .toEpochSecond(ZoneOffset.UTC),
           lon.toDouble,
           lat.toDouble)
      })
  }

  private def setYearAndMonthPairsReturnedFromDataSource(
      pairs: (Int, Int)*): Unit = {
    val (years, months) = pairs.unzip
    val nexts = months.map { _ =>
      true
    } :+ false

    when(pairResultSetMock.next()).thenReturn(nexts.head, nexts.tail: _*)
    when(pairResultSetMock.getInt("year"))
      .thenReturn(years.head, years.tail: _*)
    when(pairResultSetMock.getInt("month"))
      .thenReturn(months.head, months.tail: _*)
  }

  private def setDataReturnedFromDataSource(
      data: Seq[(String, Long, Double, Double)]): Unit = {
    data.map {
      case (mmsi: String, timeSeconds: Long, lon: Double, lat: Double) =>
        List(mmsi, makeTimestamp(timeSeconds), lon, lat, true)
    }.transpose match {
      case List(
          mmsi: List[String],
          timestamps: List[Timestamp],
          lon: List[Double],
          lat: List[Double],
          hasNext: List[Boolean]
          ) =>
        when(resultSetMock.getString("mmsi"))
          .thenReturn(mmsi.head, mmsi.tail: _*)
        when(resultSetMock.getTimestamp("acquisition_time"))
          .thenReturn(timestamps.head, timestamps.tail: _*)
        when(resultSetMock.getDouble("lon")).thenReturn(lon.head, lon.tail: _*)
        when(resultSetMock.getDouble("lat")).thenReturn(lat.head, lat.tail: _*)
        when(resultSetMock.next())
          .thenReturn(hasNext.head, hasNext.tail: _*)
          .thenReturn(false)

      case _ => resultSetMock.next() returns false
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
