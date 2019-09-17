package uk.gov.ukho.ais.resampler.utility

import java.sql.Timestamp

import uk.gov.ukho.ais.resampler.model.Ping

object TestPingCreator {

  def ping(mmsi: String, timestamp: Timestamp, lon: Double, lat: Double) = Ping(
    "arkposid", // arkposid: String
    mmsi,
    timestamp,
    lon,
    lat,
    "vessel_class", // vessel_class: String
    0, // message_type_id: Int
    "navigational_status", // navigational_status: String
    "rot", // rot: String
    "sog", // sog: String
    "cog", // cog: String
    "true_heading", // true_heading: String
    "altitude", // altitude: String
    "special_manoeuvre", // special_manoeuvre: String
    "radio_status", // radio_status: String
    "flags", // flags: String
    "input_ais_data_file" // input_ais_data_file: String
  )

}
