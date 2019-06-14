package uk.gov.ukho.ais.rasters

import java.sql.Timestamp
import java.util.Comparator

import org.apache.commons.math3.util.Precision
import org.assertj.core.api.Assertions.assertThat
import org.junit.{Before, Test}

import scala.collection.JavaConverters._

class InterpolatorTest {

  private final val DOUBLE_COMPARISON_PRECISION: Double = 0.00000000000000001
  private final val TIME_THRESHOLD: Long = 6 * 60 * 60 * 1000
  private final val DISTANCE_THRESHOLD: Long = 30000
  private final val START_PERIOD =
    Timestamp.valueOf("1970-01-01 00:00:00")
  private final val END_PERIOD =
    Timestamp.valueOf("3000-01-01 00:00:00")
  private final val TEST_CONFIG: Config =
    new Config("",
               "",
               "",
               true,
               0,
               TIME_THRESHOLD,
               DISTANCE_THRESHOLD,
               START_PERIOD,
               END_PERIOD)

  private val doubleComparator: Comparator[Double] = new Comparator[Double] {
    override def compare(a: Double, b: Double): Int =
      Precision.compareTo(a, b, DOUBLE_COMPARISON_PRECISION)
  }

  @Test
  def whenInterpolationOfNoPingsThenNoPingsReturned(): Unit = {
    val outPings: Seq[ShipPing] =
      Interpolator.interpolatePings(Seq[ShipPing](), TEST_CONFIG)

    assertThat(outPings.size).isEqualTo(0)
  }

  @Test
  def whenInterpolationOfOnePingThenOnePingReturned(): Unit = {
    val inPings: Seq[ShipPing] = createPings(Seq((3, 1, 1)))
    val expectedPings: Seq[ShipPing] = createPings(Seq((3, 1, 1)))

    val outPings: Seq[ShipPing] =
      Interpolator.interpolatePings(inPings, TEST_CONFIG)

    assertThatOutPingsMatchesExpectedPings(expectedPings, outPings)
  }

  @Test
  def whenNoInterpolationNeededThenOutputPingsSameAsInputPings(): Unit = {
    val inPings: Seq[ShipPing] = createPings(
      Seq((0, 0, 0), (6, 0.002, 0.002), (3, 0.001, 0.001)))
    val expectedPings: Seq[ShipPing] = createPings(
      Seq((0, 0, 0), (3, 0.001, 0.001), (6, 0.002, 0.002)))

    val outPings: Seq[ShipPing] =
      Interpolator.interpolatePings(inPings, TEST_CONFIG)

    assertThatOutPingsMatchesExpectedPings(expectedPings, outPings)
  }

  @Test
  def whenInterpolationNeededThenOutputPingsIncludesInterpolatedPings()
    : Unit = {
    val inPings: Seq[ShipPing] = createPings(
      Seq((0, 0, 0), (6, 0.02, 0.02), (9, 0.03, 0.03)))
    val expectedPings: Seq[ShipPing] = createPings(
      Seq((0, 0, 0), (3, 0.01, 0.01), (6, 0.02, 0.02), (9, 0.03, 0.03)))

    val outPings: Seq[ShipPing] =
      Interpolator.interpolatePings(inPings, TEST_CONFIG)

    assertThatOutPingsMatchesExpectedPings(expectedPings, outPings)
  }

  @Test
  def whenInterpolationOfGapOver6HoursThenGapNotInterpolated(): Unit = {
    val inPings: Seq[ShipPing] = createPings(
      Seq((0, 0, 0),
          (6, 0.02, 0.02),
          (9, 0.03, 0.03),
          (9990, 0.08, 0.08),
          (9996, 0.1, 0.1)))
    val expectedPings: Seq[ShipPing] = createPings(
      Seq((0, 0, 0),
          (3, 0.01, 0.01),
          (6, 0.02, 0.02),
          (9, 0.03, 0.03),
          (9990, 0.08, 0.08),
          (9993, 0.09, 0.09),
          (9996, 0.1, 0.1)))

    val outPings: Seq[ShipPing] =
      Interpolator.interpolatePings(inPings, TEST_CONFIG)

    assertThatOutPingsMatchesExpectedPings(expectedPings, outPings)
  }

  @Test
  def whenInterpolationOfGapOver6HoursWhenSecondJourneyNotOn3MinuteBoundaryThenGapNotInterpolated()
    : Unit = {
    val inPings: Seq[ShipPing] = createPings(
      Seq((0, 0, 0),
          (6, 0.02, 0.02),
          (9, 0.03, 0.03),
          (9991, 0.08, 0.08),
          (9997, 0.1, 0.1)))
    val expectedPings: Seq[ShipPing] = createPings(
      Seq((0, 0, 0),
          (3, 0.01, 0.01),
          (6, 0.02, 0.02),
          (9, 0.03, 0.03),
          (9991, 0.08, 0.08),
          (9994, 0.09, 0.09),
          (9997, 0.1, 0.1)))

    val outPings: Seq[ShipPing] =
      Interpolator.interpolatePings(inPings, TEST_CONFIG)

    assertThatOutPingsMatchesExpectedPings(expectedPings, outPings)
  }

  @Test
  def whenInterpolationOfPingsAllOver6HoursGapThenGapNotInterpolated(): Unit = {
    val inPings: Seq[ShipPing] = createPings(
      Seq((0, 0, 0), (361, 0.02, 0.02), (722, 0.03, 0.03)))
    val expectedPings: Seq[ShipPing] = createPings(
      Seq((0, 0, 0), (361, 0.02, 0.02), (722, 0.03, 0.03)))

    val outPings: Seq[ShipPing] =
      Interpolator.interpolatePings(inPings, TEST_CONFIG)

    assertThatOutPingsMatchesExpectedPings(expectedPings, outPings)
  }

  @Test
  def whenInterpolationOfDataAtNon3MinuteGapsThenResampledCorrectly(): Unit = {
    val inPings: Seq[ShipPing] = createPings(
      Seq((0, 0, 0),
          (1, 0.01, 0.01),
          (2, 0.02, 0.02),
          (4, 0.04, 0.04),
          (7, 0.07, 0.07)))
    val expectedPings: Seq[ShipPing] = createPings(
      Seq((0, 0, 0), (3, 0.03, 0.03), (6, 0.06, 0.06)))

    val outPings: Seq[ShipPing] =
      Interpolator.interpolatePings(inPings, TEST_CONFIG)

    assertThatOutPingsMatchesExpectedPings(expectedPings, outPings)
  }

  @Test
  def whenInterpolationOfPointsOver30kmThenNoInterpolationPerformed(): Unit = {
    val inPings: Seq[ShipPing] = createPings(
      Seq((0, 51.310567, -3.380413), (4, 51.312013, -3.822284)))
    val expectedPings: Seq[ShipPing] = createPings(
      Seq((0, 51.310567, -3.380413), (4, 51.312013, -3.822284)))

    val outPings: Seq[ShipPing] =
      Interpolator.interpolatePings(inPings, TEST_CONFIG)

    assertThatOutPingsMatchesExpectedPings(expectedPings, outPings)
  }

  private def createPings(pings: Seq[(Double, Double, Double)]): Seq[ShipPing] = {
    pings.map {
      case (min, lat, lon) => ShipPing("mmsi1", (min * 60000).toLong, lat, lon)
    }
  }

  private def assertThatOutPingsMatchesExpectedPings(
      expectedPings: Seq[ShipPing],
      outPings: Seq[ShipPing]) = {
    assertThat(outPings.asJava.toArray)
      .usingFieldByFieldElementComparator()
      .usingComparatorForElementFieldsWithType(doubleComparator,
                                               classOf[Double])
      .containsExactlyElementsOf(expectedPings.asJava)
  }
}
