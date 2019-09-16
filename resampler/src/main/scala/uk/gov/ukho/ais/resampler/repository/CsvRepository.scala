package uk.gov.ukho.ais.resampler.repository

import java.io.{BufferedOutputStream, File, FileOutputStream}

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.PutObjectRequest
import org.apache.commons.io.IOUtils
import uk.gov.ukho.ais.resampler.Config
import uk.gov.ukho.ais.resampler.model.Ping
import uk.gov.ukho.ais.resampler.service.CsvS3KeyService

object CsvRepository {

  def writePingsForMonth(year: Int, month: Int, pings: Iterator[Ping])(
    implicit config: Config,
    s3Client: AmazonS3): Unit = {

    val directory =
      if (config.isLocal) config.outputDirectory
      else s"/tmp"

    pings
      .grouped(25 * 1E6.toInt)
      .zipWithIndex
      .foreach {
        case (pings, part: Int) =>
          val fileName = generateFilename(year, month, part)

          val filePath = s"$directory/$fileName.csv.bz2"
          val file = new File(filePath)

          val pbzip2 = new ProcessBuilder("/bin/bash", "-c", s"pbzip2 -c > ${file.getAbsolutePath}")
            .start()

          val outputStream = pbzip2.getOutputStream

          pings
            .zipWithIndex
            .foreach {
              case (ping, i: Int) =>
                IOUtils.write(ping.toString, outputStream)

                if (i % 1E6 == 0)
                  println(f"wrote ${i / 1E6}m pings for year $year, month $month to $filePath")
            }

          outputStream.close()
          pbzip2.waitFor()

          if (!config.isLocal) uploadFileToS3AndDelete(year, month, file)
      }
  }

  private def uploadFileToS3AndDelete(year: Int,
                                      month: Int,
                                      localFileToUpload: File)(
                                       implicit config: Config,
                                       s3Client: AmazonS3): Unit = {
    println(
      s"uploading file '${localFileToUpload.getAbsolutePath}' " +
        s"for year $year, month $month to ${config.outputDirectory}...")

    s3Client.putObject(
      new PutObjectRequest(
        config.outputDirectory,
        CsvS3KeyService.generateS3Key(year, month, 0),
        localFileToUpload
      )
    )

    localFileToUpload.delete()
  }

  private def generateFilename(year: Int, month: Int, part: Int): String =
    f"$year-$month-part-$part%05d"
}
