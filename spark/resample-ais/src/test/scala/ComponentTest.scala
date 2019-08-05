import java.io.{BufferedReader, File, FileInputStream, InputStreamReader}
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path}

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream
import org.apache.commons.io.FileUtils
import org.assertj.core.api.Assertions.assertThat
import org.junit.{After, AfterClass, Before, Ignore, Test}
import uk.gov.ukho.ais.resample.ResampleAis
import uk.gov.ukho.ais.{ResourceService, Session}

import scala.collection.JavaConverters._

object ComponentTest {
  @AfterClass
  def afterAll(): Unit =
    ResourceService.cleanUp
}

class ComponentTest {
  Session.init("Partition Raw AIS", isTestSession = true)

  var tempOutputDir: File = _

  @Before
  def beforeEach(): Unit = {
    val path: Path = Files.createTempDirectory("resample-test")
    tempOutputDir = path.toFile
  }

  @After
  def afterEach(): Unit =
    FileUtils.forceDelete(tempOutputDir)

  @Test
  def whenGivenPingsWithDstApplicableDatesThenOutputDatesAreInUtc(): Unit = {
    val inputDirPath: String =
      ResourceService.copyFilesToFileSystem(
        "/tz_test/year=2018/month=1/day=1/ais_sample_with_dst.txt"
      )

    ResampleAis.main(
      Array(
        "-i",
        inputDirPath,
        "-o",
        tempOutputDir.getAbsolutePath,
        "--timeThreshold",
        "21600000",
        "--distanceThreshold",
        "30000"
      )
    )

    val expectedTimestamps = Seq[String](
      "2017-01-01 00:00:50",
      "2017-01-02 00:00:50",
      "2017-02-01 00:00:20",
      "2018-03-01 00:00:10",
      "2018-04-01 00:00:00",
      "2018-04-02 00:00:00",
      "2019-04-01 00:00:30",
      "2019-05-01 00:00:40"
    )

    val dir: File = new File(
      s"${tempOutputDir.getAbsolutePath}/year=2018/month=1/day=1")
    val actualTimestamps: Seq[String] = FileUtils
      .listFiles(dir, Array("bz2"), false)
      .stream()
      .iterator()
      .asScala
      .flatMap(bzFilename =>
        readBzippedFile(bzFilename)
          .map(line => line.split("\t")(2)))
      .toSeq

    assertThat(actualTimestamps.iterator.asJava)
      .containsExactlyInAnyOrderElementsOf(expectedTimestamps.asJava)
  }

  @Test
  def whenResamplingPartitionedInputThenCorrectOutputReturned(): Unit = {
    val inputDirPath: String =
      ResourceService.copyFilesToFileSystem(
        "/partitioned_data/year=2017/month=1/day=1/2017.txt",
        "/partitioned_data/year=2018/month=1/day=1/2018.txt",
        "/partitioned_data/year=2019/month=1/day=1/2019.txt"
      )

    ResampleAis.main(
      Array(
        "-i",
        inputDirPath,
        "-o",
        tempOutputDir.getAbsolutePath,
        "--timeThreshold",
        "21600000",
        "--distanceThreshold",
        "30000"
      )
    )

    val expected2019Lines: Seq[String] = Seq(
      "180207074437634\t3456799\t2019-01-01 00:00:00\t1.0\t2.0\tA\t1\t0\t0\t0.1\t352.2\t320\t-1\t0\t59916\t1024\twibble"
    )

    val expected2018Lines: Seq[String] = Seq(
      "180207074437634\t3456790\t2018-01-01 00:00:00\t1.0\t2.0\tA\t1\t0\t0\t0.1\t352.2\t320\t-1\t0\t59916\t1024\twibble",
      "180207074437635\t3456790\t2018-01-01 00:12:00\t1.00208\t2.00208\tA\t1\t0\t0\t0.1\t352.2\t320\t-1\t0\t59916\t1024\twibble",
      "180207074437635\t3456790\t2018-01-01 00:57:00\t1.00388\t2.00388\tA\t1\t0\t0\t0.1\t352.2\t320\t-1\t0\t59916\t1024\twibble",
      "180207074437636\t3456790\t2018-01-01 01:00:00\t1.004\t2.004\tA\t1\t0\t0\t0.1\t352.2\t320\t-1\t0\t59916\t1024\twibble",
      "180207074437637\t3456790\t2018-01-01 02:57:00\t1.0079\t2.0079\tA\t1\t0\t0\t0.1\t352.2\t320\t-1\t0\t59916\t1024\twibble",
      "180207074437638\t3456790\t2018-01-01 03:00:00\t1.008\t2.008\tA\t1\t0\t0\t0.1\t352.2\t320\t-1\t0\t59916\t1024\twibble"
    )

    val expected2017Lines: Seq[String] = Seq(
      "180207074437631\t3456793\t2017-01-01 00:00:00\t1.0\t2.0\tA\t1\t0\t0\t0.1\t352.2\t320\t-1\t0\t59916\t1024\twibble",
      "180207074437631\t3456793\t2017-01-01 00:03:00\t1.0005538461538461\t2.000553846153846\tA\t1\t0\t0\t0.1\t352.2\t320\t-1\t0\t59916\t1024\twibble",
      "180207074437631\t3456793\t2017-01-01 00:06:00\t1.0011076923076923\t2.0011076923076923\tA\t1\t0\t0\t0.1\t352.2\t320\t-1\t0\t59916\t1024\twibble",
      "180207074437631\t3456793\t2017-01-01 00:09:00\t1.0016615384615384\t2.0016615384615384\tA\t1\t0\t0\t0.1\t352.2\t320\t-1\t0\t59916\t1024\twibble",
      "180207074437632\t3456793\t2017-01-01 00:12:00\t1.0022456140350877\t2.0022456140350875\tA\t1\t0\t0\t0.1\t352.2\t320\t-1\t0\t59916\t1024\twibble",
      "180207074437632\t3456793\t2017-01-01 00:15:00\t1.002877192982456\t2.002877192982456\tA\t1\t0\t0\t0.1\t352.2\t320\t-1\t0\t59916\t1024\twibble",
      "180207074437632\t3456793\t2017-01-01 00:18:00\t1.0035087719298246\t2.0035087719298246\tA\t1\t0\t0\t0.1\t352.2\t320\t-1\t0\t59916\t1024\twibble"
    )

    assertThat(getLinesFromFiles("/year=2017/month=1/day=1").asJava)
      .as("Contains correct 2017 data")
      .containsExactlyElementsOf(expected2017Lines.asJava)

    assertThat(getLinesFromFiles("/year=2018/month=1/day=1").asJava)
      .as("Contains correct 2018 data")
      .containsAll(expected2018Lines.asJava)

    assertThat(getLinesFromFiles("/year=2019/month=1/day=1").asJava)
      .as("Contains correct 2019 data")
      .containsExactlyElementsOf(expected2019Lines.asJava)
  }

  private def getLinesFromFiles(dirName: String): Iterator[String] = {
    val dir: File = new File(s"${tempOutputDir.getAbsolutePath}$dirName")
    val actualLines: Seq[String] = FileUtils
      .listFiles(dir, Array("bz2"), false)
      .stream()
      .iterator()
      .asScala
      .flatMap(bzFilename => readBzippedFile(bzFilename))
      .toSeq
    actualLines.iterator
  }

  private def readBzippedFile(bzippedFile: File): Iterator[String] = {
    new BufferedReader(
      new InputStreamReader(
        new BZip2CompressorInputStream(new FileInputStream(bzippedFile)),
        StandardCharsets.UTF_8
      )
    ).lines().iterator.asScala
  }
}
