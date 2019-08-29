package uk.gov.ukho.ais.heatmaps

import java.io.File

object FileUtilities {
  def findGeneratedFiles(tempPath: String): Array[String] =
    new File(tempPath)
      .listFiles()
      .filter(file => file.isFile)
      .map(file => file.getName)
}
