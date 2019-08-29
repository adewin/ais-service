package uk.gov.ukho.ais.heatmaps.processor

import java.util.Comparator

import uk.gov.ukho.ais.heatmaps.model.Ping
import uk.gov.ukho.ais.heatmaps.utility.TestPingCreator.ping
import org.apache.commons.math3.util.Precision
import org.assertj.core.api.SoftAssertions
import org.junit.Test

import scala.collection.JavaConverters._
import scala.collection.mutable.ArrayBuffer
import uk.gov.ukho.ais.heatmaps.processor.YearMonthFilter.Filter

class YearMonthFilterTest {

  private final val DOUBLE_COMPARISON_PRECISION: Double = 0.00000000001

  @Test
  def whenListOfPingsIsFilteredByYearAndMonthThenOnlyPingsWithinMonthAreReturned()
    : Unit =
    SoftAssertions.assertSoftly { softly =>
      val inPings = ArrayBuffer[Ping](
        ping("", 2018, 1, 1, 1.1, 2.2),
        ping("", 2018, 1, 30, 1.1, 2.2),
        ping("", 2018, 2, 1, 1.1, 2.2),
        ping("", 2019, 1, 1, 1.1, 2.2)
      )

      val actualPings: Iterator[Ping] =
        inPings.iterator.filterPingsByYearAndMonth(2018, 1)

      val expectedPings = ArrayBuffer[Ping](
        ping("", 2018, 1, 1, 1.1, 2.2),
        ping("", 2018, 1, 30, 1.1, 2.2)
      )

      softly
        .assertThat(actualPings.asJava)
        .usingFieldByFieldElementComparator()
        .usingComparatorForElementFieldsWithType(doubleComparator,
                                                 classOf[Double])
        .containsExactlyElementsOf(expectedPings.asJava)

    }

  private val doubleComparator: Comparator[Double] =
    (a: Double, b: Double) => {
      Precision.compareTo(a, b, DOUBLE_COMPARISON_PRECISION)
    }
}
