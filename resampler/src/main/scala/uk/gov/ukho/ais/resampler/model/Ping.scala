package uk.gov.ukho.ais.resampler.model

import java.sql.Timestamp

case class Ping(
    mmsi: String,
    acquisitionTime: Timestamp,
    longitude: Double,
    latitude: Double
)
