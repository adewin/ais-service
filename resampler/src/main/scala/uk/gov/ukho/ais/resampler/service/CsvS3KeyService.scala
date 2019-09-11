package uk.gov.ukho.ais.resampler.service

import java.util.concurrent.TimeUnit

import uk.gov.ukho.ais.resampler.Config

object CsvS3KeyService {

  def generateS3Key(year: Int, month: Int, part: Int)(
      implicit config: Config): String = {
    val interpolationDescriptor = createInterpolationDescriptor(config)

    val objectPrefix =
      s"resample=$interpolationDescriptor/year=$year/month=$month/"
    val filename = f"part-$part%06d.csv.bz2"

    s"$objectPrefix$filename"
  }

  private def createInterpolationDescriptor(config: Config): String = {
    val interpolationTimeHours = TimeUnit.MILLISECONDS.toHours(
      config.interpolationTimeThresholdMilliseconds)
    val interpolationDistanceKms = config.interpolationDistanceThresholdMeters / 1000
    s"${interpolationTimeHours}hr-${interpolationDistanceKms}km"
  }
}
