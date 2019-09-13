package uk.gov.ukho.ais.resampler.processor

import geotrellis.raster.{IntArrayTile, RasterExtent}
import org.assertj.core.api.SoftAssertions
import org.junit.Test
import uk.gov.ukho.ais.resampler.Config
import uk.gov.ukho.ais.resampler.model.Ping
import uk.gov.ukho.ais.resampler.utility.TestPingCreator.ping
import uk.gov.ukho.ais.resampler.utility.TimeUtilities.makeTimestamp

import scala.collection.mutable.ArrayBuffer

class RasterGeneratorTest {
  implicit val config: Config = Config.default.copy(isLocal = true)

  @Test
  def whenNoPingsAddedThenRasterOfZerosReturned(): Unit =
    SoftAssertions.assertSoftly { softly =>
      val (_, intArrayTile) = RasterGenerator.mapToRaster(List.empty.iterator)

      softly.assertThat(intArrayTile.findMinMax._1).isEqualTo(0)
      softly.assertThat(intArrayTile.findMinMax._2).isEqualTo(0)
    }

  @Test
  def whenListOfPingsIsGivenThenCorrectRasterIsReturned(): Unit =
    SoftAssertions.assertSoftly { softly =>
      val inPings: ArrayBuffer[Ping] = ArrayBuffer(
        ping("", makeTimestamp(0), 179.0, 89.9D),
        ping("", makeTimestamp(0), 179.0, 89.9D),
        ping("", makeTimestamp(0), 179.0, 0D),
        ping("", makeTimestamp(0), 179.0, 10D),
        ping("", makeTimestamp(0), -179.0, 10D)
      )

      RasterGenerator.mapToRaster(inPings.iterator) match {
        case (extent: RasterExtent, matrix: IntArrayTile) =>
          softly.assertThat(matrix.findMinMax._1).isEqualTo(0)
          softly.assertThat(matrix.findMinMax._2).isEqualTo(2)

          val gridPoints1 = extent.mapToGrid(179D, 89.9D)
          softly
            .assertThat(matrix.get(gridPoints1._1, gridPoints1._2))
            .isEqualTo(2)

          val gridPoints2 = extent.mapToGrid(179D, 0D)
          softly
            .assertThat(matrix.get(gridPoints2._1, gridPoints2._2))
            .isEqualTo(1)

          val gridPoints3 = extent.mapToGrid(179D, 10D)
          softly
            .assertThat(matrix.get(gridPoints3._1, gridPoints3._2))
            .isEqualTo(1)

          val gridPoints4 = extent.mapToGrid(-179D, 10D)
          softly
            .assertThat(matrix.get(gridPoints4._1, gridPoints4._2))
            .isEqualTo(1)

          softly.assertThat(matrix.toArray().sum).isEqualTo(5)
        case _ => softly.fail("No raster created")
      }
    }
}
