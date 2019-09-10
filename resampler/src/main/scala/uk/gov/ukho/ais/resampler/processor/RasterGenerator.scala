package uk.gov.ukho.ais.resampler.processor

import geotrellis.raster.{CellSize, IntArrayTile, RasterExtent}
import geotrellis.vector.Extent
import uk.gov.ukho.ais.resampler.Config
import uk.gov.ukho.ais.resampler.model.Ping

object RasterGenerator {

  def mapToRaster(pings: Iterator[Ping])(
      implicit config: Config): (RasterExtent, IntArrayTile) = {
    val resolution = config.resolution

    val rasterExtent = RasterExtent(
      Extent(-180, -90, 180, 90).expandBy(resolution),
      CellSize(resolution, resolution)
    )

    val rasterMatrix =
      IntArrayTile.fill(0, rasterExtent.cols, rasterExtent.rows)

    pings
      .foreach { ping =>
        val (col, row) = rasterExtent.mapToGrid(ping.longitude, ping.latitude)
        rasterMatrix.set(col, row, rasterMatrix.get(col, row) + 1)
      }

    (rasterExtent, rasterMatrix)
  }
}
