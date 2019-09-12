package uk.gov.ukho.ais.resampler

import java.io.File

object FileUtilities {
  def findGeneratedFiles(tempPath: String): Array[String] = {
    val files = new File(tempPath)
      .listFiles()
      .filter(file => file.isFile)
      .map(file => file.getName)

    println("found generated files: " + files.mkString(", "))

    files
  }
}
