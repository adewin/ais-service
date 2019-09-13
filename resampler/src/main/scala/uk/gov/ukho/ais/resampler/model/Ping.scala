package uk.gov.ukho.ais.resampler.model

import java.sql.Timestamp

case class Ping(arkposid: String,
                mmsi: String,
                acquisitionTime: Timestamp,
                longitude: Double,
                latitude: Double,
                vesselClass: String,
                messageTypeId: Int,
                navigational_status: String,
                rot: String,
                sog: String,
                cog: String,
                true_heading: String,
                altitude: String,
                special_manoeuvre: String,
                radio_status: String,
                flags: String,
                input_ais_data_file: String) {
  override def toString: String =
    s"""$arkposid\t$mmsi\t$acquisitionTime\t$longitude\t$latitude\t$vesselClass\t$messageTypeId\t
       |$navigational_status\t$rot\t$sog\t$cog\t$true_heading\t$altitude\t$special_manoeuvre\t$radio_status\t$flags\t
       |$input_ais_data_file""".stripMargin
}
