package uk.gov.ukho.ais.heatmaps.generator.model

import java.sql.Timestamp

case class Ping(
    mmsi: String,
    acquisitionTime: Timestamp,
    longitude: Double,
    latitude: Double
)
