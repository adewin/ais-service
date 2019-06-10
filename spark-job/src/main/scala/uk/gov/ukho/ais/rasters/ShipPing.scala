package uk.gov.ukho.ais.rasters

import java.sql.Timestamp
import java.time.Instant

case class ShipPing(
    final val mmsi: String,
    final val acquisitionTime: Long,
    final val latitude: Double,
    final val longitude: Double
) {
  override def toString: String =
    s"ShipPing[$mmsi,${Timestamp.from(Instant.ofEpochMilli(acquisitionTime))}, $longitude, $latitude]"
}