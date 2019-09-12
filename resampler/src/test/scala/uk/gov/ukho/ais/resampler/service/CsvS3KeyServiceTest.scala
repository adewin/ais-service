package uk.gov.ukho.ais.resampler.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import uk.gov.ukho.ais.resampler.Config

class CsvS3KeyServiceTest {

  @Test
  def whenConfigSuppliedThenS3KeyIncludesPartitions(): Unit = {
    val interpolationTimeThresholdMilliseconds = 6 * 60 * 60 * 1000
    val interpolationDistanceThresholdMeters = 30000

    implicit val config: Config = Config.default.copy(
      inputFiles = Seq("s3://test/input-ais.tar.bz2"),
      interpolationDistanceThresholdMeters =
        interpolationDistanceThresholdMeters,
      interpolationTimeThresholdMilliseconds =
        interpolationTimeThresholdMilliseconds
    )

    val generatedPath = CsvS3KeyService.generateS3Key(2019, 1, 0)

    assertThat(generatedPath).isEqualTo(
      "30km_6hr/year=2019/month=1/part-00000.csv.bz2")
  }
}
