package uk.gov.ukho.ais.partitioning

import java.io.{File, FileFilter}
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path, Paths}

import org.apache.commons.io.FileUtils
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
