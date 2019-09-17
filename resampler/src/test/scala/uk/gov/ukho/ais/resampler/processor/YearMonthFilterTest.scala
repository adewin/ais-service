package uk.gov.ukho.ais.resampler.processor

import java.time.LocalDate
import java.util.Comparator

import org.apache.commons.math3.util.Precision
import org.assertj.core.api.SoftAssertions
import org.junit.Test
import uk.gov.ukho.ais.resampler.model.Ping
import uk.gov.ukho.ais.resampler.processor.YearMonthFilter.Filter
import uk.gov.ukho.ais.resampler.utility.TestPingCreator.ping
import uk.gov.ukho.ais.resampler.utility.TimeUtilities.makeTimestampFromLocalDateTime

import scala.collection.JavaConverters._
import scala.collection.mutable.ArrayBuffer

class YearMonthFilterTest {

  private final val DOUBLE_COMPARISON_PRECISION: Double = 0.00000000001

  @Test
  def whenListOfPingsIsFilteredByYearAndMonthThenOnlyPingsWithinMonthAreReturned()
    : Unit =
    SoftAssertions.assertSoftly { softly =>
      val inPings = ArrayBuffer[Ping](
        pingWithYearMonthAndDay("", 2018, 1, 1, 1.1, 2.2),
        pingWithYearMonthAndDay("", 2018, 1, 30, 1.1, 2.2),
        pingWithYearMonthAndDay("", 2018, 2, 1, 1.1, 2.2),
        pingWithYearMonthAndDay("", 2019, 1, 1, 1.1, 2.2)
      )

      val actualPings: Iterator[Ping] =
        inPings.iterator.filterPingsByYearAndMonth(2018, 1)

      val expectedPings = ArrayBuffer[Ping](
        pingWithYearMonthAndDay("", 2018, 1, 1, 1.1, 2.2),
        pingWithYearMonthAndDay("", 2018, 1, 30, 1.1, 2.2)
      )

      softly
        .assertThat(actualPings.asJava)
        .usingFieldByFieldElementComparator()
        .usingComparatorForElementFieldsWithType(doubleComparator,
                                                 classOf[Double])
        .containsExactlyElementsOf(expectedPings.asJava)

    }

  private def pingWithYearMonthAndDay(mmsi: String,
                                      year: Int,
                                      month: Int,
                                      day: Int,
                                      lat: Double,
                                      lon: Double) =
    ping(mmsi,
         makeTimestampFromLocalDateTime(
           LocalDate.of(year, month, day).atStartOfDay()),
         lat,
         lon)

  private val doubleComparator: Comparator[Double] =
    (a: Double, b: Double) => {
      Precision.compareTo(a, b, DOUBLE_COMPARISON_PRECISION)
    }
}
