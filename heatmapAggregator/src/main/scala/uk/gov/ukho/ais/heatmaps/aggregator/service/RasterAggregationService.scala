package uk.gov.ukho.ais.heatmaps.aggregator.service

import java.io.File
import java.nio.file.Files

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.PutObjectRequest
import geotrellis.raster.Tile
import geotrellis.raster.io.geotiff.reader.GeoTiffReader
import geotrellis.raster.io.geotiff.{GeoTiff, SinglebandGeoTiff}
import geotrellis.spark.io.s3.AmazonS3Client
import geotrellis.spark.io.s3.util.S3RangeReader
import geotrellis.util.StreamingByteReader
import uk.gov.ukho.ais.heatmaps.aggregator.Config

object RasterAggregationService {
  private final val S3_CHUNK_SIZE_100MB = 104857600

  def performRasterAggregation(inputPaths: List[String], outputPath: String)(
      implicit config: Config,
      s3Client: AmazonS3): Unit = {
    println(s"Creating raster aggregation at $outputPath from:")
    inputPaths.foreach(println)

    println(s"Reading: ${inputPaths.head}")
    val headTiff: SinglebandGeoTiff = readGeoTiff(inputPaths.head)
    var outputTile: Tile = headTiff.tile

    inputPaths.tail.foreach { path: String =>
      println(s"Reading: $path")
      val inputTile: Tile = readGeoTiff(path).tile

      println("Combining with previous...")
      outputTile = outputTile.dualCombine(inputTile)(_ + _)(_ + _)
    }

    println("Combined rasters, creating geotiff...")

    val outputTiff: SinglebandGeoTiff =
      GeoTiff(outputTile, headTiff.extent, headTiff.crs)
    val tempOutputFile: File =
      Files.createTempFile("aggregator-", ".tif").toFile
    tempOutputFile.deleteOnExit()

    outputTiff.write(tempOutputFile.getAbsolutePath)
    uploadFileToS3(tempOutputFile, outputPath)

    println("Written to S3 successfully")
  }

  private def uploadFileToS3(localFileToUpload: File, outputPath: String)(
      implicit config: Config,
      s3Client: AmazonS3): Unit = {
    s3Client.putObject(
      new PutObjectRequest(
        config.heatmapsDirectory,
        outputPath,
        localFileToUpload
      ))
  }

  def readGeoTiff(path: String)(implicit config: Config,
                                s3Client: AmazonS3): SinglebandGeoTiff = {

    GeoTiffReader.readSingleband(
      StreamingByteReader(
        S3RangeReader(s"s3://${config.heatmapsDirectory}/$path",
                      new AmazonS3Client(s3Client)),
        S3_CHUNK_SIZE_100MB))
  }
}
