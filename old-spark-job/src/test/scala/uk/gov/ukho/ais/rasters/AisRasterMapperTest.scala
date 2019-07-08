package uk.gov.ukho.ais.rasters

import java.time.Instant

import geotrellis.raster.{IntArrayTile, RasterExtent}
import org.apache.spark.rdd.RDD
import org.assertj.core.api.Assertions.{assertThat, fail}
import org.junit.Test
import uk.gov.ukho.ais.rasters.AisRasterMapper.ShipPingRasterMapper

class AisRasterMapperTest {

  private final val RESOLUTION = 1D
  private final val TOTAL_CELL_COUNT_WHOLE_WORLD_AT_1K = 65884
  private final val TEST_CONFIG: Config = ConfigParser.parse(
    Array(
      "-i",
      "",
      "-o",
      "",
      "-p",
      "",
      "-r",
      s"$RESOLUTION",
      "-t",
      "3000",
      "-d",
      "300000",
      "-s",
      "1970-01-01",
      "-e",
      "3000-01-01",
      "--draughtConfigFile",
      "",
      "--staticDataFile",
      ""
    ))

  Session.init(true)

  @Test
  def whenMappingShipPingsThenRasterAndMatrixCreated(): Unit = {

    val inputRdd: RDD[ShipPing] = Session.sparkSession.sparkContext.parallelize(
      Seq(
        ShipPing("mmsi1", Instant.now.toEpochMilli, 89.9D, 179.0, -1D, 1),
        ShipPing("mmsi1", Instant.now.toEpochMilli, 89.9D, 179.0, -1D, 1),
        ShipPing("mmsi1", Instant.now.toEpochMilli, 0D, 179.0, -1D, 1),
        ShipPing("mmsi1", Instant.now.toEpochMilli, 10D, 179.0, -1D, 1),
        ShipPing("mmsi1", Instant.now.toEpochMilli, 10D, -179.0, -1D, 1)
      ))

    inputRdd.mapToRaster(TEST_CONFIG) match {
      case (extent: RasterExtent, matrix: IntArrayTile) =>
        assertThat(extent.size).isEqualTo(TOTAL_CELL_COUNT_WHOLE_WORLD_AT_1K)
        assertThat(matrix.findMinMax).isEqualTo((0, 2))

        val gridPoints1 = extent.mapToGrid(179D, 89.9D)
        assertThat(matrix.get(gridPoints1._1, gridPoints1._2)).isEqualTo(2)

        val gridPoints2 = extent.mapToGrid(179D, 0D)
        assertThat(matrix.get(gridPoints2._1, gridPoints2._2)).isEqualTo(1)

        val gridPoints3 = extent.mapToGrid(179D, 10D)
        assertThat(matrix.get(gridPoints3._1, gridPoints3._2)).isEqualTo(1)

        val gridPoints4 = extent.mapToGrid(-179D, 10D)
        assertThat(matrix.get(gridPoints4._1, gridPoints4._2)).isEqualTo(1)

        assertThat(matrix.toArray().sum).isEqualTo(5)
      case _ => fail("No raster created")
    }
  }
}
