package uk.gov.ukho.ais

import java.io.{File, InputStream}
import java.nio.file.{Files, Path, Paths, StandardCopyOption}

import org.apache.commons.io.FileUtils

import scala.collection.mutable

object ResourceService {

  var filesToCleanUp: mutable.MutableList[Path] = mutable.MutableList()

  def copyFileToFileSystem(filename: String): String = {
    val resourceInputStream = getClass.getResourceAsStream(s"/$filename")
    val tempFile: File = File.createTempFile("scalatest", filename)

    checkInputStream(filename, resourceInputStream)

    Files.copy(resourceInputStream,
               tempFile.toPath,
               StandardCopyOption.REPLACE_EXISTING)

    tempFile.deleteOnExit()
    tempFile.getAbsolutePath
  }

  def copyFilesToFileSystem(filenames: String*): String = {
    val tempDir: Path = Files.createTempDirectory("scalatest")
    filesToCleanUp += tempDir

    filenames.foreach { filename =>
      val subPath: Path = Paths.get(filename)
      val resourceInputStream = getClass.getResourceAsStream(subPath.toString)

      checkInputStream(filename, resourceInputStream)

      val subDir: Path = Paths.get(tempDir.toString, subPath.getParent.toString)
      subDir.toFile.mkdirs()

      Files.copy(resourceInputStream,
                 Paths.get(subDir.toString, subPath.getFileName.toString),
                 StandardCopyOption.REPLACE_EXISTING)
    }

    tempDir.toAbsolutePath.toString
  }

  def cleanUp: Unit = {
    filesToCleanUp.foreach(path =>
      FileUtils.deleteDirectory(path.toAbsolutePath.toFile))
  }

  private def checkInputStream(filename: String,
                               resourceInputStream: InputStream) = {
    if (resourceInputStream == null) {
      throw new IllegalArgumentException(
        s"File: $filename does not exist or cannot be read")
    }
  }
}
