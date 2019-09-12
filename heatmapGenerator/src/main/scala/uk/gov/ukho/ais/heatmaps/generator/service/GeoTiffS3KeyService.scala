package uk.gov.ukho.ais.heatmaps.generator.service

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

import org.apache.commons.io.FilenameUtils
import uk.gov.ukho.ais.heatmaps.generator.Config

object GeoTiffS3KeyService {

  def generateS3Key()(implicit config: Config): String = {
    val interpolationDescriptor = createInterpolationDescriptor(config)
    val dateDescriptor = createDateDescriptor(config)
    val sqlFilenameDescriptor = createSqlFilenameDescriptor(config)

    val objectPrefix = s"sqlFilename=$sqlFilenameDescriptor.sql/resample=$interpolationDescriptor/type=monthly" +
      s"/year=${config.year}/month=${config.month}/"
    val filename =
      s"$sqlFilenameDescriptor-1km-res-$interpolationDescriptor-monthly-$dateDescriptor.tif"

    s"$objectPrefix$filename"
  }

  private def createSqlFilenameDescriptor(config: Config): String =
    FilenameUtils.getBaseName(config.filterSqlFile.split("/").last)

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
