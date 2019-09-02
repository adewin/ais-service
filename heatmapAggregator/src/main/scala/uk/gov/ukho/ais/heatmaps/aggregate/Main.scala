package uk.gov.ukho.ais.heatmaps.aggregate

import java.io.File
import java.nio.file.{Files, Paths}

import geotrellis.proj4.CRS
import geotrellis.raster.io.geotiff.{GeoTiff, SinglebandGeoTiff}
import geotrellis.raster.io.geotiff.reader.GeoTiffReader
import geotrellis.raster.io.geotiff.writer.GeoTiffWriter
import geotrellis.vector.Extent


object Main {

  val OUTPUT_CRS: CRS = CRS.fromName("EPSG:4326")

  def main(args: Array[String]): Unit = {

    val tifPath = "/home/joel/tifs/cropped.tif"
    val tifOutput = "/home/joel/tifs/summed.tif"

    val tif1 = GeoTiffReader.readSingleband(tifPath, Extent(-180, -90, 180, 90).expandBy(1))
    print("read 1")
    val tif2 = GeoTiffReader.readSingleband(tifPath, Extent(-180, -90, 180, 90).expandBy(1))
    print("read 2")

    val summed = tif1.tile.combine(tif2.tile)(_ + _)
    print("summed")
    val summedTif = GeoTiff(summed, Extent(-180, -90, 180, 90).expandBy(1), OUTPUT_CRS)
    print("Created tif")

    GeoTiffWriter.write(summedTif, tifOutput)
  }
}