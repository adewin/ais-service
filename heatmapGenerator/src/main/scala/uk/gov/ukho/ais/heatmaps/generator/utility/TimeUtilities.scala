package uk.gov.ukho.ais.heatmaps.generator.utility

import java.sql.Timestamp
import java.time.{Instant, LocalDate, LocalDateTime, ZoneOffset}

object TimeUtilities {
  def getNextMonth(year: Int, month: Int): (Int, Int) = {
    val newDate = LocalDate.of(year, month, 1).plusMonths(1)

    (newDate.getYear, newDate.getMonthValue)
  }

  def getLastDayOfPreviousMonth(year: Int, month: Int): (Int, Int, Int) = {
    val newDate = LocalDate.of(year, month, 1).minusDays(1)

    (newDate.getYear, newDate.getMonthValue, newDate.getDayOfMonth)
  }

  def makeTimestamp(secondsSinceEpoch: Long): Timestamp =
    Timestamp.from(Instant.ofEpochMilli(secondsSinceEpoch * 1000))

  def makeTimestampFromLocalDateTime(localDateTime: LocalDateTime): Timestamp =
    Timestamp.from(localDateTime.toInstant(ZoneOffset.UTC))
}
