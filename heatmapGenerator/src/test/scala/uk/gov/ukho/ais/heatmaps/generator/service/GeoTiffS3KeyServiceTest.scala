package uk.gov.ukho.ais.heatmaps.generator.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import uk.gov.ukho.ais.heatmaps.generator.Config

class GeoTiffS3KeyServiceTest {

  @Test
  def whenConfigSuppliedThenS3KeyIncludesPartitions(): Unit = {
    val interpolationTimeThresholdMilliseconds = 3 * 60 * 60 * 1000
    val interpolationDistanceThresholdMeters = 3000

    implicit val config: Config = Config.default.copy(
      filterSqlFile = "s3://test/unfiltered.sql.1",
      interpolationDistanceThresholdMeters =
        interpolationDistanceThresholdMeters,
      interpolationTimeThresholdMilliseconds =
        interpolationTimeThresholdMilliseconds,
      month = 1,
      year = 2019
    )

    val generatedPath = GeoTiffS3KeyService.generateS3Key()

    assertThat(generatedPath).isEqualTo(
      "sqlFilename=unfiltered.sql.1/resample=3hr-3km/type=monthly/year=2019/month=1/unfiltered.sql.1-1km-res-3hr-3km-monthly-Jan-2019.tif")
  }
}
