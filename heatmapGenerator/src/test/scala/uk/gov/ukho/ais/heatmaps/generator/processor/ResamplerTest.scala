package uk.gov.ukho.ais.heatmaps.generator.processor

import java.sql.Timestamp
import java.time.Instant
import java.util.Comparator

import org.apache.commons.math3.util.Precision
import org.assertj.core.api.SoftAssertions
import org.junit.Test
import uk.gov.ukho.ais.heatmaps.generator.Config
import uk.gov.ukho.ais.heatmaps.generator.model.Ping

import scala.collection.JavaConverters._
import scala.collection.mutable.ArrayBuffer

class ResamplerTest {

  private final val DOUBLE_COMPARISON_PRECISION: Double = 0.00000000001
  private final val TIME_THRESHOLD: Long = 6 * 60 * 60 * 1000
  private final val DISTANCE_THRESHOLD: Long = 30000
  implicit val config: Config = Config.default.copy(
    isLocal = true,
    interpolationTimeThresholdMilliseconds = TIME_THRESHOLD,
    interpolationDistanceThresholdMeters = DISTANCE_THRESHOLD
  )

  @Test
  def whenResamplingNoPingsThenNoPingsReturned(): Unit =
    SoftAssertions.assertSoftly { softly =>
      val inPings: ArrayBuffer[Ping] = ArrayBuffer()
      val outPings: Iterator[Ping] = Resampler.resamplePings(inPings.iterator)

      softly.assertThat(outPings.size).isEqualTo(0)
    }

  @Test
  def whenResamplingOnePingThenOnePingReturned(): Unit =
    SoftAssertions.assertSoftly { softly =>
      val inPings: ArrayBuffer[Ping] = createPings(ArrayBuffer((3, 1, 1)))
      val expectedPings: ArrayBuffer[Ping] = createPings(ArrayBuffer((3, 1, 1)))

      val outPings = Resampler.resamplePings(inPings.iterator)

      softly
        .assertThat(outPings.asJava)
        .usingFieldByFieldElementComparator()
        .usingComparatorForElementFieldsWithType(doubleComparator,
                                                 classOf[Double])
        .containsExactlyElementsOf(expectedPings.asJava)
    }

  @Test
  def whenNoResamplingNeededThenOutputPingsSameAsInputPings(): Unit =
    SoftAssertions.assertSoftly { softly =>
      val inPings: ArrayBuffer[Ping] = createPings(
        ArrayBuffer((0, 0, 0), (3, 0.001, 0.001), (6, 0.002, 0.002)))
      val expectedPings: ArrayBuffer[Ping] = createPings(
        ArrayBuffer((0, 0, 0), (3, 0.001, 0.001), (6, 0.002, 0.002)))

      val outPings: Iterator[Ping] = Resampler.resamplePings(inPings.iterator)

      softly
        .assertThat(outPings.asJava)
        .usingFieldByFieldElementComparator()
        .usingComparatorForElementFieldsWithType(doubleComparator,
                                                 classOf[Double])
        .containsExactlyElementsOf(expectedPings.asJava)
    }

  @Test
  def whenResamplingNeededThenOutputPingsIncludesResampledPings(): Unit =
    SoftAssertions.assertSoftly { softly =>
      val inPings: ArrayBuffer[Ping] =
        createPings(ArrayBuffer((0, 0, 0), (6, 0.02, 0.02), (9, 0.03, 0.03)))
      val expectedPings: ArrayBuffer[Ping] = createPings(
        ArrayBuffer((0, 0, 0),
                    (3, 0.01, 0.01),
                    (6, 0.02, 0.02),
                    (9, 0.03, 0.03)))

      val outPings: Iterator[Ping] = Resampler.resamplePings(inPings.iterator)

      softly
        .assertThat(outPings.asJava)
        .usingFieldByFieldElementComparator()
        .usingComparatorForElementFieldsWithType(doubleComparator,
                                                 classOf[Double])
        .containsExactlyElementsOf(expectedPings.asJava)
    }

  @Test
  def whenResamplingPingsWithGapOverTimeBoundaryThenGapNotResampled(): Unit =
    SoftAssertions.assertSoftly { softly =>
      val inPings: ArrayBuffer[Ping] = createPings(
        ArrayBuffer((0, 0, 0),
                    (6, 0.02, 0.02),
                    (9, 0.03, 0.03),
                    (9990, 0.08, 0.08),
                    (9996, 0.1, 0.1)))
      val expectedPings: ArrayBuffer[Ping] = createPings(
        ArrayBuffer((0, 0, 0),
                    (3, 0.01, 0.01),
                    (6, 0.02, 0.02),
                    (9, 0.03, 0.03),
                    (9990, 0.08, 0.08),
                    (9993, 0.09, 0.09),
                    (9996, 0.1, 0.1)))

      val outPings: Iterator[Ping] = Resampler.resamplePings(inPings.iterator)

      softly
        .assertThat(outPings.asJava)
        .usingFieldByFieldElementComparator()
        .usingComparatorForElementFieldsWithType(doubleComparator,
                                                 classOf[Double])
        .containsExactlyElementsOf(expectedPings.asJava)
    }

  @Test
  def whenResamplingPingsWithGapOver6HoursWhenSecondJourneyNotOnTimeBoundaryThenGapNotResampled()
    : Unit = SoftAssertions.assertSoftly { softly =>
    val inPings: ArrayBuffer[Ping] = createPings(
      ArrayBuffer((0, 0, 0),
                  (6, 0.02, 0.02),
                  (9, 0.03, 0.03),
                  (9991, 0.08, 0.08),
                  (9997, 0.1, 0.1)))
    val expectedPings: ArrayBuffer[Ping] = createPings(
      ArrayBuffer((0, 0, 0),
                  (3, 0.01, 0.01),
                  (6, 0.02, 0.02),
                  (9, 0.03, 0.03),
                  (9991, 0.08, 0.08),
                  (9994, 0.09, 0.09),
                  (9997, 0.1, 0.1)))

    val outPings: Iterator[Ping] = Resampler.resamplePings(inPings.iterator)

    softly
      .assertThat(outPings.asJava)
      .usingFieldByFieldElementComparator()
      .usingComparatorForElementFieldsWithType(doubleComparator,
                                               classOf[Double])
      .containsExactlyElementsOf(expectedPings.asJava)
  }

  @Test
  def whenResamplingPingsAllOverTimeBoundaryGapThenGapNotResampled(): Unit =
    SoftAssertions.assertSoftly { softly =>
      val inPings: ArrayBuffer[Ping] = createPings(
        ArrayBuffer((0, 0, 0), (361, 0.02, 0.02), (722, 0.03, 0.03)))
      val expectedPings: ArrayBuffer[Ping] = createPings(
        ArrayBuffer((0, 0, 0), (361, 0.02, 0.02), (722, 0.03, 0.03)))

      val outPings: Iterator[Ping] = Resampler.resamplePings(inPings.iterator)

      softly
        .assertThat(outPings.asJava)
        .usingFieldByFieldElementComparator()
        .usingComparatorForElementFieldsWithType(doubleComparator,
                                                 classOf[Double])
        .containsExactlyElementsOf(expectedPings.asJava)
    }

  @Test
  def whenResamplingDataAtNon3MinuteGapsThenResampledCorrectly(): Unit =
    SoftAssertions.assertSoftly { softly =>
      val inPings: ArrayBuffer[Ping] = createPings(
        ArrayBuffer((0, 0, 0),
                    (1, 0.01, 0.01),
                    (2, 0.02, 0.02),
                    (4, 0.04, 0.04),
                    (7, 0.07, 0.07)))
      val expectedPings: ArrayBuffer[Ping] =
        createPings(ArrayBuffer((0, 0, 0), (3, 0.03, 0.03), (6, 0.06, 0.06)))

      val outPings: Iterator[Ping] = Resampler.resamplePings(inPings.iterator)

      softly
        .assertThat(outPings.asJava)
        .usingFieldByFieldElementComparator()
        .usingComparatorForElementFieldsWithType(doubleComparator,
                                                 classOf[Double])
        .containsExactlyElementsOf(expectedPings.asJava)
    }

  @Test
  def whenResamplingPointsOver30kmThenNoResamplingPerformed(): Unit =
    SoftAssertions.assertSoftly { softly =>
      val inPings: ArrayBuffer[Ping] = createPings(
        ArrayBuffer((0, 51.310567, -3.380413), (4, 51.312013, -3.822284)))
      val expectedPings: ArrayBuffer[Ping] = createPings(
        ArrayBuffer((0, 51.310567, -3.380413), (4, 51.312013, -3.822284)))

      val outPings: Iterator[Ping] = Resampler.resamplePings(inPings.iterator)

      softly
        .assertThat(outPings.asJava)
        .usingFieldByFieldElementComparator()
        .usingComparatorForElementFieldsWithType(doubleComparator,
                                                 classOf[Double])
        .containsExactlyElementsOf(expectedPings.asJava)
    }

  private def createPings(
      seq: ArrayBuffer[(Long, Double, Double)]): ArrayBuffer[Ping] = {
    seq
      .map {
        case (min: Long, lat: Double, lon: Double) =>
          Ping(
            "MMSI",
            Timestamp.from(Instant.EPOCH.plusSeconds(min * 60)),
            lon,
            lat
          )
      }
  }

  private val doubleComparator: Comparator[Double] =
    (a: Double, b: Double) => {
      Precision.compareTo(a, b, DOUBLE_COMPARISON_PRECISION)
    }
}
