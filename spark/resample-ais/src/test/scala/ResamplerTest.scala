import java.sql.Timestamp
import java.time.Instant
import java.util.Comparator

import org.apache.commons.math3.util.Precision
import org.apache.spark.sql.{DataFrame, Row}
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import uk.gov.ukho.ais.resample.ConfigParser
import uk.gov.ukho.ais.resample.ResamplingFilter._
import uk.gov.ukho.ais.{Schema, Session}

import scala.collection.JavaConverters._

class ResamplerTest {

  private final val DOUBLE_COMPARISON_PRECISION: Double = 0.00000000000000001
  private final val TIME_THRESHOLD: Long = 6 * 60 * 60 * 1000
  private final val DISTANCE_THRESHOLD: Long = 30000
  private final val START_PERIOD = "1970-01-01"
  private final val END_PERIOD = "3000-01-01"
  private final val TEST_CONFIG = ConfigParser.parse(
    Array(
      "-i",
      "",
      "-o",
      "",
      "-t",
      s"$TIME_THRESHOLD",
      "-d",
      s"$DISTANCE_THRESHOLD"
    ))

  Session.init("", isTestSession = true)

  @Test
  def whenResamplingNoPingsThenNoPingsReturned(): Unit = {
    val inputDataFrame: DataFrame = createDataFrame(Seq())

    val outPings: DataFrame = inputDataFrame.resample(TEST_CONFIG)

    assertThat(outPings.count()).isEqualTo(0)
  }

  @Test
  def whenResamplingOnePingThenOnePingReturned(): Unit = {
    val inputDataFrame: DataFrame = createDataFrame(Seq((3, 1, 1)))

    val expectedPings: DataFrame = createDataFrame(Seq((3, 1, 1)))

    val outPings: DataFrame = inputDataFrame.resample(TEST_CONFIG)

    assertThatOutPingsMatchesExpectedPings(expectedPings, outPings)
  }

  @Test
  def whenNoResamplingNeededThenOutputPingsSameAsInputPings(): Unit = {
    val inputDataFrame: DataFrame = createDataFrame(
      Seq((0, 0, 0), (6, 0.002, 0.002), (3, 0.001, 0.001)))
    val expectedPings: DataFrame = createDataFrame(
      Seq((0, 0, 0), (3, 0.001, 0.001), (6, 0.002, 0.002)))

    val outPings: DataFrame = inputDataFrame.resample(TEST_CONFIG)

    assertThatOutPingsMatchesExpectedPings(expectedPings, outPings)
  }

  @Test
  def whenResamplingNeededThenOutputPingsIncludesResampledPings(): Unit = {
    val inPings: DataFrame = createDataFrame(
      Seq((0, 0, 0), (6, 0.02, 0.02), (9, 0.03, 0.03)))
    val expectedPings: DataFrame = createDataFrame(
      Seq((0, 0, 0), (3, 0.01, 0.01), (6, 0.02, 0.02), (9, 0.03, 0.03)))

    val outPings: DataFrame = inPings.resample(TEST_CONFIG)

    assertThatOutPingsMatchesExpectedPings(expectedPings, outPings)
  }

  @Test
  def whenResamplingPingsWithGapOverTimeBoundaryThenGapNotResampled(): Unit = {
    val inputDataFrame: DataFrame = createDataFrame(
      Seq((0, 0, 0),
          (6, 0.02, 0.02),
          (9, 0.03, 0.03),
          (9990, 0.08, 0.08),
          (9996, 0.1, 0.1)))
    val expectedPings: DataFrame = createDataFrame(
      Seq((0, 0, 0),
          (3, 0.01, 0.01),
          (6, 0.02, 0.02),
          (9, 0.03, 0.03),
          (9990, 0.08, 0.08),
          (9993, 0.09, 0.09),
          (9996, 0.1, 0.1)))

    val outPings: DataFrame = inputDataFrame.resample(TEST_CONFIG)

    assertThatOutPingsMatchesExpectedPings(expectedPings, outPings)
  }

  @Test
  def whenResamplingPingsWithGapOver6HoursWhenSecondJourneyNotOnTimeBoundaryThenGapNotResampled()
    : Unit = {
    val inputDataFrame: DataFrame = createDataFrame(
      Seq((0, 0, 0),
          (6, 0.02, 0.02),
          (9, 0.03, 0.03),
          (9991, 0.08, 0.08),
          (9997, 0.1, 0.1)))
    val expectedPings: DataFrame = createDataFrame(
      Seq((0, 0, 0),
          (3, 0.01, 0.01),
          (6, 0.02, 0.02),
          (9, 0.03, 0.03),
          (9991, 0.08, 0.08),
          (9994, 0.09, 0.09),
          (9997, 0.1, 0.1)))

    val outPings: DataFrame = inputDataFrame.resample(TEST_CONFIG)

    assertThatOutPingsMatchesExpectedPings(expectedPings, outPings)
  }

  @Test
  def whenResamplingPingsAllOverTimeBoundaryGapThenGapNotResampled(): Unit = {
    val inputDataFrame: DataFrame = createDataFrame(
      Seq((0, 0, 0), (361, 0.02, 0.02), (722, 0.03, 0.03)))
    val expectedPings: DataFrame = createDataFrame(
      Seq((0, 0, 0), (361, 0.02, 0.02), (722, 0.03, 0.03)))

    val outPings: DataFrame = inputDataFrame.resample(TEST_CONFIG)

    assertThatOutPingsMatchesExpectedPings(expectedPings, outPings)
  }

  @Test
  def whenResamplingDataAtNon3MinuteGapsThenResampledCorrectly(): Unit = {
    val inputDataFrame: DataFrame = createDataFrame(
      Seq((0, 0, 0),
          (1, 0.01, 0.01),
          (2, 0.02, 0.02),
          (4, 0.04, 0.04),
          (7, 0.07, 0.07)))
    val expectedPings: DataFrame = createDataFrame(
      Seq((0, 0, 0), (3, 0.03, 0.03), (6, 0.06, 0.06)))

    val outPings: DataFrame = inputDataFrame.resample(TEST_CONFIG)

    assertThatOutPingsMatchesExpectedPings(expectedPings, outPings)
  }

  @Test
  def whenResamplingPointsOver30kmThenNoResamplingPerformed(): Unit = {
    val inputDataFrame: DataFrame = createDataFrame(
      Seq((0, 51.310567, -3.380413), (4, 51.312013, -3.822284)))
    val expectedPings: DataFrame = createDataFrame(
      Seq((0, 51.310567, -3.380413), (4, 51.312013, -3.822284)))

    val outPings: DataFrame = inputDataFrame.resample(TEST_CONFIG)

    assertThatOutPingsMatchesExpectedPings(expectedPings, outPings)
  }

  private def createDataFrame(seq: Seq[(Long, Double, Double)]): DataFrame = {
    val data = seq
      .map {
        case (min: Long, lat: Double, lon: Double) =>
          Row.fromTuple(
            ("ARKPOS",
             "MMSI",
             Timestamp.from(Instant.EPOCH.plusSeconds(min * 60)),
             lon,
             lat,
             "CLASS",
             1,
             "NAVSTAT",
             "ROT",
             "SOG",
             "COG",
             "HEAD",
             "ALT",
             "MANOEURVE",
             "RADSTAT",
             "FLAGS",
             "DATAFILE",
             2018,
             1,
             1))
      }
      .toList
      .asJava
    Session.sparkSession.createDataFrame(data, Schema.PARTITIONED_AIS_SCHEMA)
  }

  private val doubleComparator: Comparator[Double] = new Comparator[Double] {
    override def compare(a: Double, b: Double): Int =
      Precision.compareTo(a, b, DOUBLE_COMPARISON_PRECISION)
  }

  private def assertThatOutPingsMatchesExpectedPings(
      expectedPings: DataFrame,
      outPings: DataFrame): Unit = {

    val expectedIter = expectedPings.toLocalIterator()
    val outIter = outPings.toLocalIterator()

    while (expectedIter.hasNext) {
      assertThat(expectedIter.next.toSeq.asJava.iterator())
        .usingComparatorForType(doubleComparator, classOf[Double])
        .containsExactlyElementsOf(outIter.next.toSeq.asJava)
    }
  }
}
