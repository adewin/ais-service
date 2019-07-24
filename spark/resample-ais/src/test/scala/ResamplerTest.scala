import java.sql.Timestamp
import java.time.Instant
import java.util.Comparator

import org.apache.commons.math3.util.Precision
import org.apache.spark.rdd.RDD
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import uk.gov.ukho.ais.Session
import uk.gov.ukho.ais.resample.Resampler.RDDResampler
import uk.gov.ukho.ais.resample.{Config, ConfigParser, ShipPing}

import scala.collection.JavaConverters._

class ResamplerTest {

  private final val DOUBLE_COMPARISON_PRECISION: Double = 0.00000000000000001
  private final val TIME_THRESHOLD: Long = 6 * 60 * 60 * 1000
  private final val DISTANCE_THRESHOLD: Long = 30000
  private implicit val TEST_CONFIG: Config = ConfigParser.parse(
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

  Session.init("ResamplerTestSession", isTestSession = true)

  private val doubleComparator: Comparator[Double] = new Comparator[Double] {
    override def compare(a: Double, b: Double): Int =
      Precision.compareTo(a, b, DOUBLE_COMPARISON_PRECISION)
  }

  @Test
  def whenResamplingNoPingsThenNoPingsReturned(): Unit = {
    val inputRdd: RDD[(String, ShipPing)] = createRdd(Seq())

    val outPings: Seq[ShipPing] = inputRdd.resample.collect()

    assertThat(outPings.size).isEqualTo(0)
  }

  @Test
  def whenResamplingOnePingThenOnePingReturned(): Unit = {
    val inputRdd: RDD[(String, ShipPing)] = createRdd(Seq((3, 1, 1)))

    val expectedPings: Seq[ShipPing] = createPings(Seq((3, 1, 1)))

    val outPings: Seq[ShipPing] = inputRdd.resample.collect()

    assertThatOutPingsMatchesExpectedPings(expectedPings, outPings)
  }

  @Test
  def whenNoResamplingNeededThenOutputPingsSameAsInputPings(): Unit = {
    val inputRdd: RDD[(String, ShipPing)] = createRdd(
      Seq((0, 0, 0), (6, 0.002, 0.002), (3, 0.001, 0.001)))
    val expectedPings: Seq[ShipPing] = createPings(
      Seq((0, 0, 0), (3, 0.001, 0.001), (6, 0.002, 0.002)))

    val outPings: Seq[ShipPing] = inputRdd.resample.collect()

    assertThatOutPingsMatchesExpectedPings(expectedPings, outPings)
  }

  @Test
  def whenResamplingNeededThenOutputPingsIncludesResampledPings(): Unit = {
    val inPings: RDD[(String, ShipPing)] = createRdd(
      Seq((0, 0, 0), (6, 0.02, 0.02), (9, 0.03, 0.03)))
    val expectedPings: Seq[ShipPing] = createPings(
      Seq((0, 0, 0), (3, 0.01, 0.01), (6, 0.02, 0.02), (9, 0.03, 0.03)))

    val outPings: Seq[ShipPing] = inPings.resample.collect()

    assertThatOutPingsMatchesExpectedPings(expectedPings, outPings)
  }

  @Test
  def whenResamplingPingsWithGapOverTimeBoundaryThenGapNotResampled(): Unit = {
    val inputRdd: RDD[(String, ShipPing)] = createRdd(
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

    val outPings: Seq[ShipPing] = inputRdd.resample.collect()

    assertThatOutPingsMatchesExpectedPings(expectedPings, outPings)
  }

  @Test
  def whenResamplingPingsWithGapOver6HoursWhenSecondJourneyNotOnTimeBoundaryThenGapNotResampled()
    : Unit = {
    val inputRdd: RDD[(String, ShipPing)] = createRdd(
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

    val outPings: Seq[ShipPing] = inputRdd.resample.collect()

    assertThatOutPingsMatchesExpectedPings(expectedPings, outPings)
  }

  @Test
  def whenResamplingPingsAllOverTimeBoundaryGapThenGapNotResampled(): Unit = {
    val inputRdd: RDD[(String, ShipPing)] = createRdd(
      Seq((0, 0, 0), (361, 0.02, 0.02), (722, 0.03, 0.03)))
    val expectedPings: Seq[ShipPing] = createPings(
      Seq((0, 0, 0), (361, 0.02, 0.02), (722, 0.03, 0.03)))

    val outPings: Seq[ShipPing] = inputRdd.resample.collect()

    assertThatOutPingsMatchesExpectedPings(expectedPings, outPings)
  }

  @Test
  def whenResamplingDataAtNon3MinuteGapsThenResampledCorrectly(): Unit = {
    val inputRdd: RDD[(String, ShipPing)] = createRdd(
      Seq((0, 0, 0),
          (1, 0.01, 0.01),
          (2, 0.02, 0.02),
          (4, 0.04, 0.04),
          (7, 0.07, 0.07)))
    val expectedPings: Seq[ShipPing] = createPings(
      Seq((0, 0, 0), (3, 0.03, 0.03), (6, 0.06, 0.06)))

    val outPings: Seq[ShipPing] = inputRdd.resample.collect()

    assertThatOutPingsMatchesExpectedPings(expectedPings, outPings)
  }

  @Test
  def whenResamplingPointsOver30kmThenNoResamplingPerformed(): Unit = {
    val inputRdd: RDD[(String, ShipPing)] = createRdd(
      Seq((0, 51.310567, -3.380413), (4, 51.312013, -3.822284)))
    val expectedPings: Seq[ShipPing] = createPings(
      Seq((0, 51.310567, -3.380413), (4, 51.312013, -3.822284)))

    val outPings: Seq[ShipPing] = inputRdd.resample.collect()

    assertThatOutPingsMatchesExpectedPings(expectedPings, outPings)
  }

  private def createRdd(
      pings: Seq[(Double, Double, Double)]): RDD[(String, ShipPing)] = {

    val pingsAsShipPing = createPings(pings).map(ping => (ping.mmsi, ping))

    Session.sparkSession.sparkContext.parallelize(pingsAsShipPing)
  }

  private def createPings(pings: Seq[(Double, Double, Double)]): Seq[ShipPing] = {
    pings.map {
      case (min, lat, lon) =>
        ShipPing("arkposid1",
                 "mmsi1",
                 Timestamp.from(Instant.ofEpochMilli((min * 60000).toLong)),
                 lon,
                 lat,
                 "1",
                 0,
                 "1",
                 "0",
                 "0",
                 "0",
                 "0",
                 "0",
                 "0",
                 "0",
                 "0",
                 "file",
                 2019,
                 1,
                 1)
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
