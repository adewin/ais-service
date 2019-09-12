package uk.gov.ukho.ais.resampler.utility

import java.io.{BufferedReader, BufferedWriter, InputStream, InputStreamReader, OutputStream, OutputStreamWriter, Reader}


object IOStreamUtils {

  def roundRobinCopy(inputStream: InputStream, outputStreams: Seq[OutputStream]): Unit = {
    val bufferedReader = new BufferedReader(new InputStreamReader(inputStream))
    val bufferedWriters = outputStreams.map { outputStream =>
      new BufferedWriter(new OutputStreamWriter(outputStream))
    }

    val bufferedWritersCyclicIterator = Iterator.continually(bufferedWriters).flatten

    var line = bufferedReader.readLine()

    while (line != null) {
      bufferedWritersCyclicIterator.next().write(line)
      line = bufferedReader.readLine()
    }

    bufferedWriters.foreach(_.close())
  }
}
