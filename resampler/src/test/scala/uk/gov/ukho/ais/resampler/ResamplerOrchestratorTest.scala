package uk.gov.ukho.ais.resampler

import scala.collection.JavaConverters._

import org.assertj.core.api.SoftAssertions
import org.junit.Test

class ResamplerOrchestratorTest {

  @Test
  def whenGetMonthsToUpdateFromEmptySetThenEmptySetReturned(): Unit =
    SoftAssertions.assertSoftly { softly =>
      val updates =
        ResamplerOrchestrator.getMonthsToResampleFromModifiedMonths(Seq.empty)

      softly.assertThat(updates.toArray).isEmpty()
    }

  @Test
  def whenGetMonthsToUpdateFromSingleMonthThenItAndAdjacentMonthsReturned()
    : Unit =
    SoftAssertions.assertSoftly { softly =>
      val expectedMonths = Seq(
        (2019, 4),
        (2019, 5),
        (2019, 6)
      )
      val updates = ResamplerOrchestrator.getMonthsToResampleFromModifiedMonths(
        Seq((2019, 5)))

      softly
        .assertThat(updates.toArray)
        .containsExactlyElementsOf(expectedMonths.asJava)
    }

  @Test
  def whenGetMonthsToUpdateFromMultipleAdjacentMonthsThenMonthsReturnedAppearOnlyOnceInSet()
    : Unit =
    SoftAssertions.assertSoftly { softly =>
      val expectedMonths = Seq(
        (2019, 4),
        (2019, 5),
        (2019, 6),
        (2019, 7),
        (2019, 8)
      )
      val updates = ResamplerOrchestrator.getMonthsToResampleFromModifiedMonths(
        Seq(
          (2019, 5),
          (2019, 6),
          (2019, 7)
        ))

      softly
        .assertThat(updates.toArray)
        .containsOnlyElementsOf(expectedMonths.asJava)
    }
}
