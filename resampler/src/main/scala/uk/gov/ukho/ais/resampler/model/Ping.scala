package uk.gov.ukho.ais.resampler.model

import java.sql.Timestamp

case class Ping(arkposid: String,
                mmsi: String,
                acquisitionTime: Timestamp,
                longitude: Double,
                latitude: Double,
                vesselClass: String,
                messageTypeId: Int,
                navigationalStatus: String,
                rot: String,
                sog: String,
                cog: String,
                trueHeading: String,
                altitude: String,
                specialManoeuvre: String,
                radioStatus: String,
                flags: String,
                inputAisDataFile: String) {
  override def toString: String =
    s"$arkposid\t$mmsi\t$acquisitionTime\t$longitude\t$latitude\t$vesselClass\t$messageTypeId\t$navigationalStatus\t" +
      s"$rot\t$sog\t$cog\t$trueHeading\t$altitude\t$specialManoeuvre\t$radioStatus\t$flags\t$inputAisDataFile\n"
}
