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

  val CELL_SIZE = 0.1
  val OUTPUT_CRS: CRS = CRS.fromName("EPSG:4326")
  val RASTER_EXTENT: RasterExtent = {
    val extent = Extent(-180, -90, 180, 90)
    RasterExtent(extent, CellSize(CELL_SIZE, CELL_SIZE))
  }

  def main(args: Array[String]) {

    val local = false

    require(args.length >= 1, "Specify data file")
    val dataFile = args(0)
    require(args.length >= 2, "Specify output directory")
    val outputLocation = args(1)
    require(args.length == 2, "Too many arguments")

    val rasterMatrix = IntArrayTile.fill(0, RASTER_EXTENT.cols, RASTER_EXTENT.rows)

    val spark = SparkSession
      .builder()
      .appName("AIS to Raster")
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    val shipPoints = spark
      .read
      .schema(Schema.AIS_SCHEMA)
      .option("sep", "\t")
      .csv(dataFile)

    val geoPoints = shipPoints
      .select("lat", "lon")
      .rdd
      .filter { row =>
        !row.isNullAt(row.fieldIndex("lat")) && !row.isNullAt(row.fieldIndex("lon"))
      }
      .map { case Row(lat: Double, lon: Double) =>
        (lat, lon)
      }

    geoPoints
      .keyBy { case (lat, lon) =>
        RASTER_EXTENT.mapToGrid(lon, lat)
      }
      .aggregateByKey(0)(
        (count, _) => count + 1,
        (count1, count2) => count1 + count2
      )
      .collect()
      .foreach { case ((col, row), count) =>
        rasterMatrix.set(col, row, count)
      }

    val geotiff = GeoTiff(rasterMatrix, RASTER_EXTENT.extent, OUTPUT_CRS)
    val cm = ColorMap.fromQuantileBreaks(rasterMatrix.histogram, ColorRamps.BlueToRed)

    val timestamp = Instant.now()

    val tifName = s"raster-$timestamp.tif"
    val pngName = s"raster-$timestamp.png"

    val localTifPath = if (local) s"$outputLocation/$tifName" else s"/tmp/$tifName"
    val localPngPath = if (local) s"$outputLocation/$pngName" else s"/tmp/$pngName"

    GeoTiffWriter.write(geotiff, localTifPath)
    rasterMatrix.renderPng(cm).write(localPngPath)

    if (!local) {
      S3Client.DEFAULT.putObject(new PutObjectRequest(outputLocation, tifName, new File(localTifPath)))
      S3Client.DEFAULT.putObject(new PutObjectRequest(outputLocation, pngName, new File(localPngPath)))
    }
  }
}
