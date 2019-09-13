package uk.gov.ukho.ais.resampler.utility

import java.sql.Timestamp
import java.time.{Instant, LocalDateTime}

import uk.gov.ukho.ais.resampler.model.Ping
import uk.gov.ukho.ais.resampler.utility.TimeUtilities.makeTimestampFromLocalDateTime

object TestPingCreator {

  def ping(mmsi: String, timestamp: Timestamp, lon: Double, lat: Double) = Ping(
    "", // arkposid: String
    mmsi,
    timestamp,
    lon,
    lat,
    "", // vessel_class: String
    0,  // message_type_id: Int
    "", // navigational_status: String
    "", // rot: String
    "", // sog: String
    "", // cog: String
    "", // true_heading: String
    "", // altitude: String
    "", // special_manoeuvre: String
    "", // radio_status: String
    "", // flags: String
    "", // input_ais_data_file: String
  )

}
