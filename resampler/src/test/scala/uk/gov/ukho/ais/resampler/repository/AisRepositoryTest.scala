package uk.gov.ukho.ais.resampler.repository

import java.sql.{Connection, PreparedStatement, ResultSet}
import java.util.Comparator

import javax.sql.DataSource
import org.apache.commons.math3.util.Precision
import org.assertj.core.api.SoftAssertions
import org.junit.runner.RunWith
import org.junit.{Before, Test}
import org.mockito.ArgumentMatchers._
import org.mockito.IdiomaticMockito
import org.mockito.Mockito._
import org.mockito.captor.ArgCaptor
import org.mockito.junit.MockitoJUnitRunner
import uk.gov.ukho.ais.resampler.Config
import uk.gov.ukho.ais.resampler.repository.AisRepository._
import uk.gov.ukho.ais.resampler.utility.TestPingCreator.ping
import uk.gov.ukho.ais.resampler.utility.TimeUtilities.makeTimestamp

import scala.collection.JavaConverters._

@RunWith(classOf[MockitoJUnitRunner])
class AisRepositoryTest extends IdiomaticMockito {
  val DOUBLE_COMPARISON_PRECISION: Double = 0.00000001

  private val doubleComparator: Comparator[Double] =
    (a: Double, b: Double) =>
      Precision.compareTo(a, b, DOUBLE_COMPARISON_PRECISION)

  private implicit val config: Config = Config.default.copy(
    database = "database",
    table = "table"
  )

  val dataSourceMock: DataSource = mock[DataSource]
  val connectionMock: Connection = mock[Connection]
  val preparedStatementMock: PreparedStatement = mock[PreparedStatement]
  val resultSetMock: ResultSet = mock[ResultSet]

  @Before
  def setup(): Unit = {
    dataSourceMock.getConnection() returns connectionMock
    connectionMock.prepareStatement(any[String]) returns preparedStatementMock
    preparedStatementMock.executeQuery() returns resultSetMock

    resultSetMock.getString("arkposid") returns "arkposid"
    resultSetMock.getString("vessel_class") returns "vessel_class"
    when(resultSetMock.getString("navigational_status"))
      .thenReturn("navigational_status")
    resultSetMock.getString("rot") returns "rot"
    resultSetMock.getString("sog") returns "sog"
    resultSetMock.getString("cog") returns "cog"
    resultSetMock.getString("true_heading") returns "true_heading"
    resultSetMock.getString("altitude") returns "altitude"
    when(resultSetMock.getString("special_manoeuvre"))
      .thenReturn("special_manoeuvre")
    resultSetMock.getString("radio_status") returns "radio_status"
    resultSetMock.getString("flags") returns "flags"
    resultSetMock.getString("input_ais_data_file") returns "input_ais_data_file"
  }

  @Test
  def whenGetPingsWithDateOutOfRangeThenEmptySeqReturned(): Unit =
    SoftAssertions.assertSoftly { softly =>
      resultSetMock.next() returns false

      val results = dataSourceMock.getFilteredPingsByDate(2018, 1)

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

      val results = dataSourceMock.getFilteredPingsByDate(2019, 1)

      softly
        .assertThat(results.asJava)
        .containsExactlyElementsOf(expectedPings.asJava)
        .usingComparatorForElementFieldsWithType(doubleComparator,
                                                 classOf[Double])
    }

  @Test
  def whenFilterStatementAppliedThenTheFilterStatementIsIncludedAsSubQuery()
    : Unit = SoftAssertions.assertSoftly { softly =>
    resultSetMock.next() returns false

    val results = dataSourceMock.getFilteredPingsByDate(2018, 1)

    softly.assertThat(results.asJava).isEmpty()

    val captor = ArgCaptor[String]
    connectionMock.prepareStatement(captor) wasCalled 31.times

    softly
      .assertThat(captor.values.iterator.asJava)
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
        dataSourceMock.getDistinctYearAndMonthPairsForFiles(Seq("i-dont-exist"))

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

      val results = dataSourceMock.getDistinctYearAndMonthPairsForFiles(Seq(""))

      softly
        .assertThat(results.iterator.asJava)
        .containsExactlyElementsOf(expectedPairs.asJava)
    }
}
