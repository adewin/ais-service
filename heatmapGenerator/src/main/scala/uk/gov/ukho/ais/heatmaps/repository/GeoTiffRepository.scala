package uk.gov.ukho.ais.heatmaps.repository

import java.io.File
import java.time.format.DateTimeFormatter
import java.time.{Instant, ZoneId}
import java.util.Locale

import com.amazonaws.services.s3.model.PutObjectRequest
import com.amazonaws.services.s3.{AmazonS3, AmazonS3ClientBuilder}
import geotrellis.proj4.CRS
import geotrellis.raster.io.geotiff.GeoTiff
import geotrellis.raster.io.geotiff.writer.GeoTiffWriter
import geotrellis.raster.render.{ColorMap, ColorRamps}
import geotrellis.raster.{IntArrayTile, RasterExtent}
import uk.gov.ukho.ais.heatmaps.Config

object GeoTiffRepository {

  val OUTPUT_CRS: CRS = CRS.fromName("EPSG:4326")
  val FILENAME_TIMESTAMP_FORMATTER: DateTimeFormatter = DateTimeFormatter
    .ofPattern("YYYY-MM-dd'T'HH.mm.ss.SSSZ")
    .withLocale(Locale.UK)
    .withZone(ZoneId.of("UTC"))

  lazy val s3Client: AmazonS3 = AmazonS3ClientBuilder
    .standard()
    .build()

  def createOutputFiles(raster: (RasterExtent, IntArrayTile))(
      implicit config: Config): Unit = {
    val (rasterExtent, rasterMatrix) = raster

    val cm =
      ColorMap.fromQuantileBreaks(rasterMatrix.histogram, ColorRamps.BlueToRed)

    val filename = generateFilename(config.outputFilePrefix)
    val directory =
      if (config.isLocal) config.outputDirectory
      else s"/tmp"

    def createGeoTiff(): File = {
      val geoTiff = GeoTiff(rasterMatrix, rasterExtent.extent, OUTPUT_CRS)
      val tiffFile: File = new File(s"$directory/$filename.tif")
      GeoTiffWriter.write(geoTiff, tiffFile.getAbsolutePath)
      tiffFile
    }

    def createPng(): File = {
      val pngFile: File = new File(s"$directory/$filename.png")
      rasterMatrix.renderPng(cm).write(pngFile.getAbsolutePath)
      pngFile
    }

    val tiffFile = createGeoTiff()
    val pngFile = createPng()

    if (!config.isLocal) {
      uploadFileToS3AndDelete(config.outputDirectory, tiffFile)
      uploadFileToS3AndDelete(config.outputDirectory, pngFile)
    }
  }

  private def uploadFileToS3AndDelete(s3Directory: String,
                                      localFileToUpload: File): Unit = {
    s3Client.putObject(
      new PutObjectRequest(
        s3Directory,
        localFileToUpload.getName,
        localFileToUpload
      ))

    localFileToUpload.delete()
  }

  private def generateFilename(prefix: String): String = {
    val timestamp = FILENAME_TIMESTAMP_FORMATTER.format(Instant.now())
    s"$prefix-raster-$timestamp"
  }
}
