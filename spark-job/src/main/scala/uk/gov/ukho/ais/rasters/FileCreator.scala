package uk.gov.ukho.ais.rasters

import java.io.File
import java.time.format.DateTimeFormatter
import java.time.{Instant, ZoneId}
import java.util.Locale

import com.amazonaws.services.s3.model.PutObjectRequest
import geotrellis.proj4.CRS
import geotrellis.raster.io.geotiff.GeoTiff
import geotrellis.raster.io.geotiff.writer.GeoTiffWriter
import geotrellis.raster.render.{ColorMap, ColorRamps}
import geotrellis.raster.{IntArrayTile, RasterExtent}
import geotrellis.spark.io.s3.S3Client

object FileCreator {

  val OUTPUT_CRS: CRS = CRS.fromName("EPSG:4326")
  val FILENAME_TIMESTAMP_FORMATTER: DateTimeFormatter = DateTimeFormatter
    .ofPattern("YYYY-MM-dd'T'HH.mm.ss.SSSZ")
    .withLocale(Locale.UK)
    .withZone(ZoneId.of("UTC"))

  def createOutputFiles(raster: (RasterExtent, IntArrayTile)): Unit = {
    val (rasterExtent, rasterMatrix) = raster

    val cm =
      ColorMap.fromQuantileBreaks(rasterMatrix.histogram, ColorRamps.BlueToRed)

    val filename = generateFilename(ConfigParser.config.outputFilenamePrefix)
    val directory =
      if (ConfigParser.config.isLocal) ConfigParser.config.outputDirectory
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

    if (!ConfigParser.config.isLocal) {
      uploadFileToS3AndDelete(ConfigParser.config.outputDirectory, tiffFile)
      uploadFileToS3AndDelete(ConfigParser.config.outputDirectory, pngFile)
    }
  }

  private def uploadFileToS3AndDelete(s3Directory: String,
                                      localFileToUpload: File): Unit = {
    S3Client.DEFAULT.putObject(
      new PutObjectRequest(s3Directory,
                           localFileToUpload.getName,
                           localFileToUpload))

    localFileToUpload.delete()
  }

  private def generateFilename(prefix: String): String = {
    val timestamp = FILENAME_TIMESTAMP_FORMATTER.format(Instant.now())
    s"$prefix-raster-$timestamp"
  }
}
