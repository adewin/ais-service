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
import geotrellis.raster.{CellSize, IntArrayTile, RasterExtent}
import geotrellis.spark.io.s3.S3Client
import geotrellis.vector.Extent
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{DataFrame, Row, SparkSession}
import uk.gov.ukho.ais.rasters.Filters.RDDFilters

object AisToRaster {

  val OUTPUT_CRS: CRS = CRS.fromName("EPSG:4326")
  val FILENAME_TIMESTAMP_FORMATTER: DateTimeFormatter = DateTimeFormatter
    .ofPattern("YYYY-MM-dd'T'HH.mm.ss.SSSZ")
    .withLocale(Locale.UK)
    .withZone(ZoneId.of("UTC"))

  def main(args: Array[String]): Unit = {
    Config.parse(args) match {
      case Some(config) => generate(config)
      case _            => System.exit(1)
    }
  }

  def generate(config: Config): Unit = {
    val rasterExtent: RasterExtent = RasterExtent(
      Extent(-180, -90, 180, 90),
      CellSize(config.resolution, config.resolution)
    )

    val rasterMatrix =
      IntArrayTile.fill(0, rasterExtent.cols, rasterExtent.rows)

    val spark = SparkSession
      .builder()
      .appName("AIS to Raster")
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    val shipPoints = spark.read
      .schema(Schema.AIS_SCHEMA)
      .option("sep", "\t")
      .csv(config.inputPath)

    val geoPoints = filterShipPings(shipPoints)

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

    val filename = generateFilename(config.outputFilenamePrefix)
    val directory = if (config.isLocal) config.outputDirectory else s"/tmp"

    GeoTiffWriter.write(geoTiff, s"$directory/$filename.tif")
    rasterMatrix.renderPng(cm).write(s"$directory/$filename.png")

    if (!config.isLocal) {
      S3Client.DEFAULT.putObject(
        new PutObjectRequest(config.outputDirectory,
                             s"$filename.tif",
                             new File(s"$directory/$filename.tif")))
      S3Client.DEFAULT.putObject(
        new PutObjectRequest(config.outputDirectory,
                             s"$filename.png",
                             new File(s"$directory/$filename.png")))
    }
  }

  private def generateFilename(prefix: String): String = {
    val timestamp = FILENAME_TIMESTAMP_FORMATTER.format(Instant.now())
    s"$prefix-raster-$timestamp"
  }

  private def filterShipPings(shipPoints: DataFrame): RDD[(Double, Double)] = {
    shipPoints
      .select("lat", "lon", "message_type_id")
      .rdd
      .filterByValidMessageType()
      .map {
        case Row(lat: Double, lon: Double, _: Int) => (lat, lon)
      }
  }
}
