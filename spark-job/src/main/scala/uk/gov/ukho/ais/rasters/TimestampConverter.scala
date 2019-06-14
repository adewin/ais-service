package uk.gov.ukho.ais.rasters

import java.sql.Timestamp
import java.time.format.DateTimeFormatter
import java.time.{LocalDate, ZoneId, ZonedDateTime}

object TimestampConverter {
  private val UTC_TIME_ZONE: ZoneId = ZoneId.of("UTC")

  def convertToTimestamp(str: String, forStartPeriod: Boolean): Timestamp = {
    val parsedDate = LocalDate.parse(str, DateTimeFormatter.ISO_DATE)

    def adjustedDate =
      if (forStartPeriod) {
        dateAtStartOfDay(parsedDate).toLocalDateTime
      } else {
        dateAtEndOfDay(parsedDate).toLocalDateTime
      }

    Timestamp.valueOf(adjustedDate)
  }

  private def dateAtStartOfDay(parsedDate: LocalDate) = {
    parsedDate.atStartOfDay(UTC_TIME_ZONE)
  }

  private def dateAtEndOfDay(parsedDate: LocalDate): ZonedDateTime = {
    val nextDay = parsedDate.plusDays(1)
    dateAtStartOfDay(nextDay)
  }
}
