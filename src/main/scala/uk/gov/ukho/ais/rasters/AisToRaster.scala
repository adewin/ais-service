package uk.gov.ukho.ais.rasters

import geotrellis.proj4.CRS
import geotrellis.raster.io.geotiff.GeoTiff
import geotrellis.raster.io.geotiff.writer.GeoTiffWriter
import geotrellis.raster.render.{ColorMap, ColorRamps}
import geotrellis.raster.{CellSize, IntArrayTile, RasterExtent}
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

    require(args.length >= 1, "Specify data file")
    val dataFile = args(0)
    require(args.length >= 2, "Specify output directory")
    val outDir = args(1)
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
    GeoTiffWriter.write(geotiff, s"$outDir/test_geotif.tif")

    val cm = ColorMap.fromQuantileBreaks(rasterMatrix.histogram, ColorRamps.BlueToRed)

    rasterMatrix.renderPng(cm).write(s"$outDir/test_png.png")

  }
}
