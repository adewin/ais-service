package uk.gov.ukho.ais.resampler.utility

import java.sql.Timestamp

import uk.gov.ukho.ais.resampler.model.Ping

object TestPingCreator {

  def ping(mmsi: String, timestamp: Timestamp, lon: Double, lat: Double) = Ping(
    "arkposid",
    mmsi,
    timestamp,
    lon,
    lat,
    "vessel_class",
    0,
    "navigational_status",
    "rot",
    "sog",
    "cog",
    "true_heading",
    "altitude",
    "special_manoeuvre",
    "radio_status",
    "flags",
    "input_ais_data_file"
  )

}
