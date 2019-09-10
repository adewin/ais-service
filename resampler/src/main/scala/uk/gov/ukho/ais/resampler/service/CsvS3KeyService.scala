package uk.gov.ukho.ais.resampler.service

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

import org.apache.commons.io.FilenameUtils
import uk.gov.ukho.ais.resampler.Config

object CsvS3KeyService {

  def generateS3Key(year: Int, month: Int)(implicit config: Config): String = {
    val interpolationDescriptor = createInterpolationDescriptor(config)
    val dateDescriptor = createDateDescriptor(year, month, config)
    val sqlFilenameDescriptor = createSqlFilenameDescriptor(config)

    val objectPrefix = s"sqlFilename=$sqlFilenameDescriptor.sql/resample=$interpolationDescriptor/type=monthly" +
      s"/year=${year}/month=${month}/"
    val filename =
      s"$sqlFilenameDescriptor-1km-res-$interpolationDescriptor-monthly-$dateDescriptor.tif"

    s"$objectPrefix$filename"
  }

  private def createSqlFilenameDescriptor(config: Config): String = ""

  private def createDateDescriptor(year: Int, month: Int, config: Config): String =
    LocalDate
      .of(year, month, 1)
      .format(DateTimeFormatter.ofPattern("MMM-YYYY"))

  private def createInterpolationDescriptor(config: Config): String = {
    val interpolationTimeHours = TimeUnit.MILLISECONDS.toHours(
      config.interpolationTimeThresholdMilliseconds)
    val interpolationDistanceKms = config.interpolationDistanceThresholdMeters / 1000
    s"${interpolationTimeHours}hr-${interpolationDistanceKms}km"
  }
}
