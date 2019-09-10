package uk.gov.ukho.ais.resampler.service

import org.junit.Test
import uk.gov.ukho.ais.resampler.Config
import org.assertj.core.api.Assertions.assertThat

class CsvS3KeyServiceTest {

  @Test
  def whenConfigSuppliedThenS3KeyIncludesPartitions(): Unit = {
    val interpolationTimeThresholdMilliseconds = 3 * 60 * 60 * 1000
    val interpolationDistanceThresholdMeters = 3000

    implicit val config: Config = Config.default.copy(
      inputFiles = Seq("s3://test/input-ais.tar.bz2"),
      interpolationDistanceThresholdMeters =
        interpolationDistanceThresholdMeters,
      interpolationTimeThresholdMilliseconds =
        interpolationTimeThresholdMilliseconds
    )

    val generatedPath = CsvS3KeyService.generateS3Key(2019, 1)

    assertThat(generatedPath).isEqualTo(
      "sqlFilename=unfiltered.sql/resample=3hr-3km/type=monthly/year=2019/month=1/unfiltered-1km-res-3hr-3km-monthly-Jan-2019.tif")
  }
}
