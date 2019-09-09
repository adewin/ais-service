package uk.gov.ukho.ais.heatmaps.repository

import java.io.{BufferedInputStream, InputStream}
import java.nio.file.{Files, Paths}

import com.amazonaws.services.s3.{AmazonS3, AmazonS3URI}
import org.apache.commons.io.IOUtils
import uk.gov.ukho.ais.heatmaps.Config

class FilterQuerySqlRepository()(implicit config: Config, s3Client: AmazonS3) {

  def retrieveFilterQuery: String = {
    val fileInputStream = new BufferedInputStream(
      getFileInputStream(config.filterSqlFile))
    IOUtils
      .readLines(fileInputStream)
      .toArray
      .mkString("\n")
  }

  private def getS3FileInputStream(sqlFile: String): InputStream = {
    val s3Uri: AmazonS3URI = new AmazonS3URI(sqlFile)
    s3Client.getObject(s3Uri.getBucket, s3Uri.getKey).getObjectContent
  }

  private def getLocalFileInputStream(sqlFile: String): InputStream =
    Files.newInputStream(Paths.get(sqlFile))

  private def getFileInputStream(sqlFile: String): InputStream =
    if (config.isLocal) getLocalFileInputStream(sqlFile)
    else getS3FileInputStream(sqlFile)
}
