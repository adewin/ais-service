package uk.gov.ukho.ais.resampler.repository

import java.io._

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.PutObjectRequest
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream
import org.apache.commons.io.IOUtils
import uk.gov.ukho.ais.resampler.Config
import uk.gov.ukho.ais.resampler.model.Ping
import uk.gov.ukho.ais.resampler.service.CsvS3KeyService
import uk.gov.ukho.ais.resampler.utility.JobSet.Job
import uk.gov.ukho.ais.resampler.utility.{IOStreamUtils, JobSet}

object CsvRepository {

  case class Output(year: Int, month: Int, part: Int = 0)(implicit config: Config) {
    val localFile: File = {
      val fileName = f"$year-$month-part-$part%06d"

      val directory =
        if (config.isLocal) config.outputDirectory
        else s"/tmp"

      val filePath = s"$directory/$fileName.csv.bz2"
      new File(filePath)
    }
  }

  def buildJobSet(year: Int, month: Int, pings: Iterator[Ping])(
    implicit config: Config,
    s3Client: AmazonS3): JobSet = {
    val pipedOut = new PipedOutputStream()
    val pipedIn = new PipedInputStream(pipedOut)

    val output = Output(year, month)

    val (jobOutputStreams: Seq[OutputStream], jobs: Seq[Job]) = (0 until 8).map { i =>
      val threadPipedOut = new PipedOutputStream()
      val threadPipedIn = new PipedInputStream(threadPipedOut)

      val newOutput = output.copy(part = i)

      (threadPipedOut, () => writeAndUpload(newOutput, threadPipedIn))
    }.unzip

    JobSet(
      Seq(() => writePingsForMonth(pipedOut, pings),
        () => IOStreamUtils.roundRobinCopy(pipedIn, jobOutputStreams)) ++
        jobs
    )
  }

  private def writePingsForMonth(outputStream: OutputStream, pings: Iterator[Ping])
                                (implicit config: Config, s3Client: AmazonS3): Unit = {
    pings.zipWithIndex.foreach { case (ping, index) =>
      val line =
        s"${ping.mmsi}\t${ping.acquisitionTime}\t${ping.longitude}\t${ping.latitude}\n"

      IOUtils.write(line, outputStream)

      if (index % 1000000 == 0) {
        println(s"processed ${index / 1000000}m pings")
      }
    }
    outputStream.close()
  }

  private def writeAndUpload(output: Output, inputStream: InputStream)
                            (implicit config: Config, s3Client: AmazonS3): Unit = {

//    val bufferedReader = new BufferedReader(new InputStreamReader(inputStream))

    // create file

    // write x lines

    // upload file

    // repeat from create file


    val fileOutputStream = new BZip2CompressorOutputStream(
      new FileOutputStream(output.localFile),
      BZip2CompressorOutputStream.MAX_BLOCKSIZE)

    IOUtils.copyLarge(inputStream, fileOutputStream)
    fileOutputStream.close()

    if (!config.isLocal) uploadFileToS3AndDelete(output)
  }

  private def uploadFileToS3AndDelete(output: Output)(
    implicit config: Config,
    s3Client: AmazonS3): Unit = {
    println(
      s"uploading file '${output.localFile.getAbsolutePath}' " +
        s"for year ${output.year}, month ${output.month} to ${config.outputDirectory}...")

    s3Client.putObject(
      new PutObjectRequest(
        config.outputDirectory,
        CsvS3KeyService.generateS3Key(output.year, output.year, 0),
        output.localFile
      )
    )

    output.localFile.delete()
  }

}
