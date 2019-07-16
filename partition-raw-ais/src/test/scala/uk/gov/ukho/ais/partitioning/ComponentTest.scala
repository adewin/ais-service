package uk.gov.ukho.ais.partitioning

import java.io.{BufferedReader, File, FileFilter, FileInputStream, FileReader}
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path, Paths}

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream
import org.apache.commons.io.{FileUtils, IOUtils}
import org.assertj.core.api.Assertions.assertThat
import org.junit.{After, Before, Test}
import uk.gov.ukho.ais.{ResourceService, Session}

class ComponentTest {
  Session.init("Partition Raw AIS", isTestSession = true)

  var tempOutputDir: File = _

  @Before
  def beforeEach(): Unit = {
    val path: Path = Files.createTempDirectory("partition-raw-ais-test")
    tempOutputDir = path.toFile
  }

  @After
  def afterEach(): Unit =
    FileUtils.forceDelete(tempOutputDir)

  @Test
  def whenPingsFrom6MonthsThen6FilesCreatedInCorrectFolders(): Unit = {
    val inputFilePath: String =
      ResourceService.copyFileToFileSystem("ais_8pings.txt")

    PartitionRawAis
      .main(
        Array(
          "-i",
          inputFilePath,
          "-o",
          tempOutputDir.getAbsolutePath
        ))

    assertFolderHasSubFolders("", "year=2017", "year=2018", "year=2019")
    assertFolderHasFiles("", "_SUCCESS", "._SUCCESS.crc")

    assertFolderHasSubFolders("year=2017", "month=1", "month=2")
    assertFolderHasSubFolders("year=2018", "month=3", "month=4")
    assertFolderHasSubFolders("year=2019", "month=4", "month=5")

    assertFolderHasSubFolders("year=2017/month=1", "day=1", "day=2")
    assertFolderHasSubFolders("year=2017/month=2", "day=1")
    assertFolderHasSubFolders("year=2018/month=3", "day=1")
    assertFolderHasSubFolders("year=2018/month=4", "day=1", "day=2")
    assertFolderHasSubFolders("year=2019/month=4", "day=1")
    assertFolderHasSubFolders("year=2019/month=5", "day=1")

    assertFolderIsNotEmpty("year=2017/month=1/day=1")
    assertFolderIsNotEmpty("year=2017/month=1/day=2")
    assertFolderIsNotEmpty("year=2017/month=2/day=1")
    assertFolderIsNotEmpty("year=2018/month=3/day=1")
    assertFolderIsNotEmpty("year=2018/month=4/day=1")
    assertFolderIsNotEmpty("year=2018/month=4/day=2")
    assertFolderIsNotEmpty("year=2019/month=4/day=1")
    assertFolderIsNotEmpty("year=2019/month=5/day=1")
  }

  @Test
  def whenGivenTimestampThenTimestampIsOutputInUTCFormatAndFilenameColumnCreated()
    : Unit = {
    val inputFilePath: String =
      ResourceService.copyFileToFileSystem("ais_8pings.txt")

    PartitionRawAis
      .main(
        Array(
          "-i",
          inputFilePath,
          "-o",
          tempOutputDir.getAbsolutePath
        ))

    val dir = new File(
      s"${tempOutputDir.getAbsolutePath}/year=2017/month=1/day=1")
    val file =
      FileUtils.listFiles(dir, Array("bz2"), false).stream().findFirst().get()
    val stream = new BZip2CompressorInputStream(new FileInputStream(file))

    val split = IOUtils.toString(stream, StandardCharsets.UTF_8).split("\t")
    assertThat(split(2)).isEqualTo("01-01-2017 00:00:50")
    assertThat(split.last.stripLineEnd).isEqualTo(s"file://$inputFilePath")
  }

  private def assertFolderHasSubFolders(folder: String,
                                        subFolders: String*): Unit =
    assertThat(getDirectories(folder)).containsExactlyInAnyOrder(subFolders: _*)

  private def assertFolderHasFiles(folder: String, files: String*): Unit =
    assertThat(getFiles(folder)).containsExactlyInAnyOrder(files: _*)

  private def assertFolderIsNotEmpty(folder: String): Unit =
    assertThat(getFiles(folder)).isNotEmpty

  private def getDirectories(subFolder: String): Array[String] =
    Paths
      .get(tempOutputDir.getAbsolutePath, subFolder)
      .toFile
      .listFiles()
      .filter(_.isDirectory)
      .map(_.getName)

  private def getFiles(subFolder: String): Array[String] =
    Paths
      .get(tempOutputDir.getAbsolutePath, subFolder)
      .toFile
      .listFiles()
      .filter(_.isFile)
      .map(_.getName)
}
