package uk.gov.ukho.ais.resampler

import geotrellis.raster.Tile
import geotrellis.raster.io.geotiff.SinglebandGeoTiff
import geotrellis.vector.Extent

class CreatedTiff(private val geoTiff: SinglebandGeoTiff) {

  def tile: Tile = this.geoTiff.tile

  def calculateSumAndCount(geoTiff: SinglebandGeoTiff): (Int, Int) = {
    var sum: Int = 0
    var count: Int = 0

    geoTiff.tile.foreach(i => {
      sum += i
      count += 1
    })
    (sum, count)
  }

  def calculateSumAndCount(): (Int, Int) = calculateSumAndCount(this.geoTiff)

  def getSumInRange(minX: Double,
                    minY: Double,
                    maxX: Double,
                    maxY: Double): Int = {
    val point = geoTiff.crop(new Extent(minX, minY, maxX, maxY))
    val (sum, _) = calculateSumAndCount(point)
    sum
  }
}
