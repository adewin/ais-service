package uk.gov.ukho.ais.rasters

import geotrellis.raster.{CellSize, IntArrayTile, RasterExtent}
import geotrellis.vector.Extent
import org.apache.spark.rdd.RDD

object AisRasterMapper {

  implicit class ShipPingRasterMapper(rdd: RDD[ShipPing]) {

    def mapToRaster(): (RasterExtent, IntArrayTile) = {
      val resolution = ConfigParser.config.resolution

      val rasterExtent = RasterExtent(
        Extent(-180, -90, 180, 90).expandBy(resolution),
        CellSize(resolution, resolution)
      )

      val rasterMatrix =
        IntArrayTile.fill(0, rasterExtent.cols, rasterExtent.rows)

      rdd
        .keyBy {
          case ShipPing(_, _, latitude, longitude, _, _) =>
            rasterExtent.mapToGrid(longitude, latitude)
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

      (rasterExtent, rasterMatrix)
    }
  }
}
