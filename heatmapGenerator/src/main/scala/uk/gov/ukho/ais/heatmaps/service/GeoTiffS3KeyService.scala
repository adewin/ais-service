package uk.gov.ukho.ais.heatmaps.service

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

import uk.gov.ukho.ais.heatmaps.Config

object GeoTiffS3KeyService {

  def generateS3Key()(implicit config: Config): String = {
    val interpolationDescriptor = createInterpolationDescriptor(config)
    val dateDescriptor = createDateDescriptor(config)

    s"interpolation=$interpolationDescriptor/unfiltered-$interpolationDescriptor-$dateDescriptor.tif"
  }

  private def createDateDescriptor(config: Config): String =
    LocalDate
      .of(config.year, config.month, 1)
      .format(DateTimeFormatter.ofPattern("MMM-YYYY"))

  private def createInterpolationDescriptor(config: Config): String = {
    val interpolationTimeHours = TimeUnit.MILLISECONDS.toHours(
      config.interpolationTimeThresholdMilliseconds)
    val interpolationDistanceKms = config.interpolationDistanceThresholdMeters / 1000
    s"${interpolationTimeHours}hr-${interpolationDistanceKms}km"
  }
}
