package uk.gov.ukho.ais.rasters

import java.io.File
import java.time.Instant

import com.amazonaws.services.s3.model.PutObjectRequest
import geotrellis.proj4.CRS
import geotrellis.raster.io.geotiff.GeoTiff
import geotrellis.raster.io.geotiff.writer.GeoTiffWriter
import geotrellis.raster.render.{ColorMap, ColorRamps}
import geotrellis.raster.{CellSize, IntArrayTile, RasterExtent}
import geotrellis.spark.io.s3.S3Client
import geotrellis.vector.Extent
import org.apache.spark.sql.{Row, SparkSession}

object AisToRaster {

  val OUTPUT_CRS: CRS = CRS.fromName("EPSG:4326")

  def main(args: Array[String]): Unit = {
    Config.parse(args) match {
      case Some(config) => generate(config)
      case _            => System.exit(1)
    }
  }

  def generate(config: Config): Unit = {
    val rasterExtent: RasterExtent = {
      val extent = Extent(-180, -90, 180, 90)
      RasterExtent(extent, CellSize(config.resolution, config.resolution))
    }

    val spark = SparkSession
      .builder()
      .appName("AIS to Raster")
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    val shipPoints = spark.read
      .schema(Schema.AIS_SCHEMA)
      .option("sep", "\t")
      .csv(config.inputPath)

    val rasterMatrix =
      IntArrayTile.fill(0, rasterExtent.cols, rasterExtent.rows)

    val geoPoints = shipPoints
      .select("lat", "lon")
      .rdd
      .map {
        case Row(lat: Double, lon: Double) =>
          (lat, lon)
      }

    geoPoints
      .keyBy {
        case (lat, lon) =>
          rasterExtent.mapToGrid(lon, lat)
      }
      .aggregateByKey(0)(
        (count, _) => count + 1,
        (count1, count2) => count1 + count2
      )
      .collect()
      .foreach {
        case ((col, row), count) =>
          rasterMatrix.set(col, row, count)
      }

    val geoTiff = GeoTiff(rasterMatrix, rasterExtent.extent, OUTPUT_CRS)
    val cm =
      ColorMap.fromQuantileBreaks(rasterMatrix.histogram, ColorRamps.BlueToRed)

    val timestamp = Instant.now()

    val tifName = s"raster-$timestamp.tif"
    val pngName = s"raster-$timestamp.png"

    val localTifPath =
      if (config.isLocal) s"${config.outputDirectory}/$tifName"
      else s"/tmp/$tifName"
    val localPngPath =
      if (config.isLocal) s"${config.outputDirectory}/$pngName"
      else s"/tmp/$pngName"

    GeoTiffWriter.write(geoTiff, localTifPath)
    rasterMatrix.renderPng(cm).write(localPngPath)

    if (!config.isLocal) {
      S3Client.DEFAULT.putObject(
        new PutObjectRequest(config.outputDirectory,
                             tifName,
                             new File(localTifPath)))
      S3Client.DEFAULT.putObject(
        new PutObjectRequest(config.outputDirectory,
                             pngName,
                             new File(localPngPath)))
    }
  }
}
