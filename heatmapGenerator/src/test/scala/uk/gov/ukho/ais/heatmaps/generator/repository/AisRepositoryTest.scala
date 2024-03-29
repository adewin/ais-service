package uk.gov.ukho.ais.heatmaps.generator.repository

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
import uk.gov.ukho.ais.heatmaps.generator.utility.TestPingCreator.ping
import uk.gov.ukho.ais.heatmaps.generator.utility.TimeUtilities.makeTimestamp

import scala.collection.JavaConverters._

@RunWith(classOf[MockitoJUnitRunner])
class AisRepositoryTest {
  val DOUBLE_COMPARISON_PRECISION: Double = 0.00000001

  private val doubleComparator: Comparator[Double] =
    (a: Double, b: Double) =>
      Precision.compareTo(a, b, DOUBLE_COMPARISON_PRECISION)

  val datasourceMock: DataSource = mock(classOf[DataSource])
  val connectionMock: Connection = mock(classOf[Connection])
  val preparedStatementMock: PreparedStatement = mock(
    classOf[PreparedStatement])
  val resultSetMock: ResultSet = mock(classOf[ResultSet])
  var aisRepository: AisRepository = _
  var filterQuery =
    "SELECT * ukho_ais_data.test_data WHERE message_type_id IN (1, 2, 3, 18, 19)"

  @Before
  def setup(): Unit = {
    aisRepository = new AisRepository(datasourceMock)

    when(datasourceMock.getConnection()).thenReturn(connectionMock)
    when(connectionMock.prepareStatement(anyString()))
      .thenReturn(preparedStatementMock)
    when(preparedStatementMock.executeQuery()).thenReturn(resultSetMock)

  }

  @Test
  def whenGetPingsWithDateOutOfRangeThenEmptySeqReturned(): Unit =
    SoftAssertions.assertSoftly { softly =>
      when(resultSetMock.next()).thenReturn(false)

      val results = aisRepository.getFilteredPingsByDate(filterQuery, 2018, 1)

      softly.assertThat(results.asJava).isEmpty()
    }

  @Test
  def whenGetPingsWithDateValidThenSeqReturned(): Unit =
    SoftAssertions.assertSoftly { softly =>
      val expectedPings = Seq(
        ping("123", 10, 1.1, 2.2),
        ping("456", 20, 3.3, 4.4),
        ping("789", 30, 5.5, 6.6)
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

      val results = aisRepository.getFilteredPingsByDate(filterQuery, 2019, 1)

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

    val results = aisRepository.getFilteredPingsByDate(filterQuery, 2018, 1)

    softly.assertThat(results.asJava).isEmpty()

    val preparedStatementArgCaptor: ArgumentCaptor[String] =
      ArgumentCaptor.forClass(classOf[String])

    verify(connectionMock, times(31))
      .prepareStatement(preparedStatementArgCaptor.capture())

    softly
      .assertThat(preparedStatementArgCaptor.getAllValues.iterator())
      .allMatch { sqlStatement =>
        sqlStatement.startsWith(
          """
            |SELECT mmsi, acquisition_time, lat, lon
            |FROM (SELECT * ukho_ais_data.test_data WHERE message_type_id IN (1, 2, 3, 18, 19))
            |WHERE (
            |(year = 2018 AND month = 1)
            |""".stripMargin)
      }
  }
}
