package uk.gov.ukho.ais.heatmaps.utility

import org.assertj.core.api.SoftAssertions
import org.junit.Test

class TimeUtilitiesTest {
  @Test
  def whenGetNextMonthIsCalledThenCorrectYearAndMonthAreReturned(): Unit =
    SoftAssertions.assertSoftly { softly =>
      val (actualYear, actualMonth) = TimeUtilities.getNextMonth(2018, 1)

      softly.assertThat(actualYear).isEqualTo(2018)
      softly.assertThat(actualMonth).isEqualTo(2)
    }

  @Test
  def whenGetNextMonthIsCalledInDecemberThenCorrectYearAndMonthAreReturned()
    : Unit =
    SoftAssertions.assertSoftly { softly =>
      val (actualYear2, actualMonth2) = TimeUtilities.getNextMonth(2018, 12)

      softly.assertThat(actualYear2).isEqualTo(2019)
      softly.assertThat(actualMonth2).isEqualTo(1)
    }

  @Test
  def whenGetLastDayOfPreviousMonthIsCalledThenCorrectYearMonthAndDayAreReturned()
    : Unit =
    SoftAssertions.assertSoftly { softly =>
      val (actualYear, actualMonth, actualDay) =
        TimeUtilities.getLastDayOfPreviousMonth(2020, 3)

      softly.assertThat(actualYear).isEqualTo(2020)
      softly.assertThat(actualMonth).isEqualTo(2)
      softly.assertThat(actualDay).isEqualTo(29)
    }

  @Test
  def whenGetLastDayOfPreviousMonthInJanuaryIsCalledThenCorrectYearMonthAndDayAreReturned()
    : Unit =
    SoftAssertions.assertSoftly { softly =>
      val (actualYear, actualMonth, actualDay) =
        TimeUtilities.getLastDayOfPreviousMonth(2020, 1)

      softly.assertThat(actualYear).isEqualTo(2019)
      softly.assertThat(actualMonth).isEqualTo(12)
      softly.assertThat(actualDay).isEqualTo(31)
    }
}
