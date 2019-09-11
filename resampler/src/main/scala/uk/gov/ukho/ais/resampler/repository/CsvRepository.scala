package uk.gov.ukho.ais.resampler.repository

import java.io.{File, FileOutputStream}
import java.time.ZoneOffset

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.PutObjectRequest
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream
import org.apache.commons.io.IOUtils
import org.slf4j.{Logger, LoggerFactory}
import uk.gov.ukho.ais.resampler.model.Ping
import uk.gov.ukho.ais.resampler.service.CsvS3KeyService
import uk.gov.ukho.ais.resampler.{Config, ResamplerOrchestrator}

object CsvRepository {

  private val PART_MAX_ROW_SIZE = 100 * 100 * 100

  def writePingsForMonth(year: Int, month: Int, pings: Iterator[Ping])(
      implicit config: Config,
      s3Client: AmazonS3): Unit = {
    val fileName = generateFilename(year, month, 0)
    val directory =
      if (config.isLocal) config.outputDirectory
      else s"/tmp"

    val filePath = s"$directory/$fileName.csv.bz2"
    val file = new File(filePath)

    val outputStream = new BZip2CompressorOutputStream(
      new FileOutputStream(file))

    var count = 0

    pings.foreach { ping =>
      val acquisitionTime =
        ping.acquisitionTime.toString
      val line =
        s"${ping.mmsi}\t$acquisitionTime\t${ping.longitude}\t${ping.latitude}\n"

      IOUtils.write(line, outputStream)

      count += 1

      if (count % 10000 == 0) {
        println(
          s"wrote ${count / 1000}k pings for year $year, month $month to $filePath")
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
    f"$year-$month-part-$part%06d"
}
