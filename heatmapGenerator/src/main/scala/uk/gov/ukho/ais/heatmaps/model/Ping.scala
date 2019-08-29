package uk.gov.ukho.ais.heatmaps.model

import java.sql.Timestamp

case class Ping(
    mmsi: String,
    acquisitionTime: Timestamp,
    longitude: Double,
    latitude: Double
)
