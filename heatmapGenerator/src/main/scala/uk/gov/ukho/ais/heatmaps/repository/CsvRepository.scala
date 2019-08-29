package uk.gov.ukho.ais.heatmaps.repository

import java.io.FileOutputStream
import java.nio.file.{Path, Paths}
import java.time.ZoneOffset

import uk.gov.ukho.ais.heatmaps.model.Ping
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream
import org.apache.commons.io.IOUtils

import scala.collection.mutable.ArrayBuffer

class CsvRepository(path: Path) {

  def writePings(pings: ArrayBuffer[Ping]): Unit = {
    val file = Paths.get(path.toString, "fred.csv.bz2").toFile

    val outputStream = new BZip2CompressorOutputStream(
      new FileOutputStream(file))

    var count = 0

    pings.foreach { ping =>
      val acquisitionTime =
        ping.acquisitionTime.toInstant.atOffset(ZoneOffset.UTC).toString
      val line =
        s"${ping.mmsi}\t$acquisitionTime\t${ping.longitude}\t${ping.latitude}\n"

      IOUtils.write(line, outputStream)

      count += 1

      if (count % 100000 == 0) {
        println(s"\twrote $count pings...")
      }
    }
    outputStream.close()
  }
}
