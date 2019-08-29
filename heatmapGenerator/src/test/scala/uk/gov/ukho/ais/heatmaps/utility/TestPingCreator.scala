package uk.gov.ukho.ais.heatmaps.utility

import java.sql.Timestamp
import java.time.{Instant, LocalDateTime}

import uk.gov.ukho.ais.heatmaps.model.Ping
import uk.gov.ukho.ais.heatmaps.utility.TimeUtilities.makeTimestampFromLocalDateTime

object TestPingCreator {

  def ping(mmsi: String, min: Long, lon: Double, lat: Double) = Ping(
    mmsi,
    Timestamp.from(Instant.EPOCH.plusSeconds(min * 60)),
    lon,
    lat
  )

  def ping(mmsi: String,
           year: Int,
           month: Int,
           day: Int,
           lon: Double,
           lat: Double) = Ping(
    mmsi,
    makeTimestampFromLocalDateTime(LocalDateTime.of(year, month, day, 0, 0, 0)),
    lon,
    lat
  )

}
