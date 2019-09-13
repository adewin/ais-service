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
    val fileName = generateFilename(year, month, 0)
    val directory =
      if (config.isLocal) config.outputDirectory
      else s"/tmp"

    val filePath = s"$directory/$fileName.csv.bz2"
    val file = new File(filePath)

    val outputStream = new BufferedOutputStream(new FileOutputStream(file), 256 * 1024 * 1024)

    var count = 0

    pings.foreach { ping =>
      val acquisitionTime =
        ping.acquisitionTime.toString
      val line =
        s"${ping.mmsi}\t$acquisitionTime\t${ping.longitude}\t${ping.latitude}\n"

      IOUtils.write(line, outputStream)

      count += 1

      if (count % 100000 == 0) {
        println(
          f"wrote ${count / 100000d}%.1fm pings for year $year, month $month to $filePath")
      }
    }
    outputStream.close()

    if (!config.isLocal) uploadFileToS3AndDelete(year, month, file)
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
