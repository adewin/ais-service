package uk.gov.ukho.ais.heatmaps.service

import org.junit.Test
import uk.gov.ukho.ais.heatmaps.Config
import org.assertj.core.api.Assertions.assertThat

class GeoTiffS3KeyServiceTest {

  @Test
  def whenConfigSuppliedThenS3KeyIncludesPartitions(): Unit = {
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
      "sqlFilename=unfiltered/resample=3hr-3km/type=monthly/year=2019/month=1/unfiltered-1km-res-3hr-3km-monthly-Jan-2019.tif")
  }
}
