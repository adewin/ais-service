package uk.gov.ukho.ais.partitioning

import java.io.{File, FileFilter}
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
  def afterEach(): Unit = {
    FileUtils.forceDelete(tempOutputDir)
  }

  @Test
  def whenPingsFrom6MonthsThen6FilesCreatedInCorrectFolders(): Unit = {
    val inputFilePath: String =
      ResourceService.copyFileToFileSystem("ais_6pings.txt")

    new PartitionRawAis()
      .main(
        Array(
          "-i",
          inputFilePath,
          "-o",
          tempOutputDir.getAbsolutePath
        ))

    val inputFileName: String = new File(inputFilePath).getName

    assertFolderHasSubFoldersAndFiles("",
                                      false,
                                      "year=2017",
                                      "year=2018",
                                      "year=2019")
    assertFolderHasSubFoldersAndFiles("year=2017", false, "month=1", "month=2")
    assertFolderHasSubFoldersAndFiles("year=2018", false, "month=3", "month=4")
    assertFolderHasSubFoldersAndFiles("year=2019", false, "month=4", "month=5")

    Array[String](
      "year=2017/month=1",
      "year=2017/month=2",
      "year=2018/month=3",
      "year=2018/month=4",
      "year=2019/month=4",
      "year=2019/month=5"
    ).foreach(subDir => {
      assertFolderHasSubFoldersAndFiles(subDir, false, inputFileName)
      assertFolderHasSubFoldersAndFiles(subDir + s"/$inputFileName",
                                        hasFiles = true)
    })
  }

  private def assertFolderHasSubFoldersAndFiles(folder: String,
                                                hasFiles: Boolean,
                                                subFolders: String*): Unit = {
    val (directories: Array[String], files: Array[String]) =
      getFilesAndDirectories(folder)
    assertThat(directories).containsExactlyInAnyOrder(subFolders: _*)
    assertThat(files.length != 0).isEqualTo(hasFiles)
  }

  private def getFilesAndDirectories(
      subFolder: String): (Array[String], Array[String]) = {
    val dir: File = Paths.get(tempOutputDir.getAbsolutePath, subFolder).toFile

    val directories: Array[String] = dir
      .listFiles(new FileFilter {
        override def accept(file: File): Boolean = file.isDirectory
      })
      .map(file => file.getName)

    val files: Array[String] = dir
      .listFiles(new FileFilter {
        override def accept(file: File): Boolean = file.isFile
      })
      .map(file => file.getName)
    (directories, files)
  }
}
