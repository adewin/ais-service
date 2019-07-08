package uk.gov.ukho.ais

import java.io.File
import java.nio.file.{Files, StandardCopyOption}

object ResourceService {

  def copyFileToFileSystem(filename: String): String = {
    val resourceInputStream = getClass.getResourceAsStream(s"/$filename")
    val tempFile: File = File.createTempFile("scalatest", filename)

    if (resourceInputStream == null) {
      throw new IllegalArgumentException(
        s"File: $filename does not exist or cannot be read")
    }

    Files.copy(resourceInputStream,
               tempFile.toPath,
               StandardCopyOption.REPLACE_EXISTING)

    tempFile.deleteOnExit()
    tempFile.getAbsolutePath
  }
}
