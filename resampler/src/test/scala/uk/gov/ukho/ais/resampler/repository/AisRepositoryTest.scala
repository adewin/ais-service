package uk.gov.ukho.ais.resampler.repository

import java.sql.{Connection, PreparedStatement, ResultSet}
import java.util.Comparator

import javax.sql.DataSource
import org.apache.commons.math3.util.Precision
import org.assertj.core.api.SoftAssertions
import org.junit.runner.RunWith
import org.junit.{Before, Test}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import org.mockito.junit.MockitoJUnitRunner
import uk.gov.ukho.ais.resampler.Config
import uk.gov.ukho.ais.resampler.utility.TestPingCreator.ping
import uk.gov.ukho.ais.resampler.utility.TimeUtilities.makeTimestamp

import scala.collection.JavaConverters._

@RunWith(classOf[MockitoJUnitRunner])
class AisRepositoryTest {
  val DOUBLE_COMPARISON_PRECISION: Double = 0.00000001

  private val doubleComparator: Comparator[Double] =
    (a: Double, b: Double) =>
      Precision.compareTo(a, b, DOUBLE_COMPARISON_PRECISION)

  private implicit val config: Config = Config.default.copy(
    database = "database",
    table = "table"
  )

  val datasourceMock: DataSource = mock(classOf[DataSource])
  val connectionMock: Connection = mock(classOf[Connection])
  val preparedStatementMock: PreparedStatement = mock(
    classOf[PreparedStatement])
  val resultSetMock: ResultSet = mock(classOf[ResultSet])
  var aisRepository: AisRepository = _

  @Before
  def setup(): Unit = {
    aisRepository = new AisRepository(datasourceMock)

    when(datasourceMock.getConnection()).thenReturn(connectionMock)
    when(connectionMock.prepareStatement(anyString()))
      .thenReturn(preparedStatementMock)
    when(preparedStatementMock.executeQuery()).thenReturn(resultSetMock)

    when(resultSetMock.getString("arkposid")).thenReturn("arkposid")
    when(resultSetMock.getString("vessel_class")).thenReturn("vessel_class")
    when(resultSetMock.getString("navigational_status"))
      .thenReturn("navigational_status")
    when(resultSetMock.getString("rot")).thenReturn("rot")
    when(resultSetMock.getString("sog")).thenReturn("sog")
    when(resultSetMock.getString("cog")).thenReturn("cog")
    when(resultSetMock.getString("true_heading")).thenReturn("true_heading")
    when(resultSetMock.getString("altitude")).thenReturn("altitude")
    when(resultSetMock.getString("special_manoeuvre"))
      .thenReturn("special_manoeuvre")
    when(resultSetMock.getString("radio_status")).thenReturn("radio_status")
    when(resultSetMock.getString("flags")).thenReturn("flags")
    when(resultSetMock.getString("input_ais_data_file"))
      .thenReturn("input_ais_data_file")
  }

  @Test
  def whenGetPingsWithDateOutOfRangeThenEmptySeqReturned(): Unit =
    SoftAssertions.assertSoftly { softly =>
      when(resultSetMock.next()).thenReturn(false)

      val results = aisRepository.getFilteredPingsByDate(2018, 1)

      softly.assertThat(results.asJava).isEmpty()
    }

  @Test
  def whenGetPingsWithDateValidThenSeqReturned(): Unit =
    SoftAssertions.assertSoftly { softly =>
      val expectedPings = Seq(
        ping("123", makeTimestamp(10 * 60), 1.1, 2.2),
        ping("456", makeTimestamp(20 * 60), 3.3, 4.4),
        ping("789", makeTimestamp(30 * 60), 5.5, 6.6)
      )

      when(resultSetMock.next()).thenReturn(true, true, true, false)
      when(resultSetMock.getString("mmsi")).thenReturn("123", "456", "789")
      when(resultSetMock.getTimestamp("acquisition_time")).thenReturn(
        makeTimestamp(10 * 60),
        makeTimestamp(20 * 60),
        makeTimestamp(30 * 60)
      )

      when(resultSetMock.getDouble("lon")).thenReturn(1.1, 3.3, 5.5)
      when(resultSetMock.getDouble("lat")).thenReturn(2.2, 4.4, 6.6)

      val results = aisRepository.getFilteredPingsByDate(2019, 1)

      softly
        .assertThat(results.asJava)
        .containsExactlyElementsOf(expectedPings.asJava)
        .usingComparatorForElementFieldsWithType(doubleComparator,
                                                 classOf[Double])
    }

  @Test
  def whenFilterStatementAppliedThenTheFilterStatementIsIncludedAsSubQuery()
    : Unit = SoftAssertions.assertSoftly { softly =>
    when(resultSetMock.next()).thenReturn(false)

    val results = aisRepository.getFilteredPingsByDate(2018, 1)

    softly.assertThat(results.asJava).isEmpty()

    val preparedStatementArgCaptor: ArgumentCaptor[String] =
      ArgumentCaptor.forClass(classOf[String])

    verify(connectionMock, times(31))
      .prepareStatement(preparedStatementArgCaptor.capture())

    softly
      .assertThat(preparedStatementArgCaptor.getAllValues.iterator())
      .allMatch { sqlStatement =>
        sqlStatement.startsWith("""
            |SELECT *
            |FROM "database"."table"
            |WHERE (
            |(year = 2018 AND month = 1)
            |""".stripMargin)
      }
  }

  @Test
  def whenGetYearAndMonthPairsForNonExistentFileThenIteratorDoesNotHaveNext()
    : Unit =
    SoftAssertions.assertSoftly { softly =>
      when(resultSetMock.next()).thenReturn(false)

      val months =
        aisRepository.getDistinctYearAndMonthPairsForFiles(Seq("i-dont-exist"))

      softly.assertThat(months.size).isEqualTo(0)
    }

  @Test
  def whenGetYearAndMonthPairsForFileThenIteratorReturned(): Unit =
    SoftAssertions.assertSoftly { softly =>
      val expectedPairs = (1 to 3).map(month => (2019, month))
      val (years, months) = expectedPairs.unzip

      when(resultSetMock.next()).thenReturn(true, true, true, false)
      when(resultSetMock.getInt("year")).thenReturn(years.head, years.tail: _*)
      when(resultSetMock.getInt("month"))
        .thenReturn(months.head, months.tail: _*)

      val results = aisRepository.getDistinctYearAndMonthPairsForFiles(Seq(""))

      softly
        .assertThat(results.iterator.asJava)
        .containsExactlyElementsOf(expectedPairs.asJava)
    }
}
