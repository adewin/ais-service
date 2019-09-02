package uk.gov.ukho.ais.heatmaps.service

import org.junit.Test
import uk.gov.ukho.ais.heatmaps.Config
import org.assertj.core.api.Assertions.assertThat

class GeoTiffS3KeyServiceTest {

  @Test
  def whenConfigSpecifiesInterpolationThresholdsThenS3KeyIncludesInterpolationPartition()
    : Unit = {
    val interpolationTimeThresholdMilliseconds = 3 * 60 * 60 * 1000
    val interpolationDistanceThresholdMeters = 3000

    implicit val config: Config = Config.default.copy(
      interpolationDistanceThresholdMeters =
        interpolationDistanceThresholdMeters,
      interpolationTimeThresholdMilliseconds =
        interpolationTimeThresholdMilliseconds,
      month = 1,
      year = 2019
    )

    val generatedPath = GeoTiffS3KeyService.generateS3Key()

    assertThat(generatedPath).isEqualTo(
      "interpolation=3hr-3km/unfiltered-3hr-3km-Jan-2019.tif")
  }
}
