package uk.gov.ukho.ais.resample

import java.sql.Timestamp

import org.apache.spark.sql.Row

object ShipPing {

  def fromRow(row: Row): ShipPing = {
    row match {
      case Row(arkPosId: String,
               mmsi: String,
               acquisitionTime: Timestamp,
               lon: Double,
               lat: Double,
               vesselClass: String,
               messageTypeId: Integer,
               navigationStatus: String,
               rateOfTurn: String,
               speedOverGround: String,
               courseOverGround: String,
               trueHeading: String,
               altitude: String,
               specialManouveure: String,
               radioStatus: String,
               flags: String,
               inputAisDataFile: String,
               year: Integer,
               month: Integer,
               day: Integer) =>
        ShipPing(
          arkPosId,
          mmsi,
          acquisitionTime,
          lon,
          lat,
          vesselClass,
          messageTypeId,
          navigationStatus,
          rateOfTurn,
          speedOverGround,
          courseOverGround,
          trueHeading,
          altitude,
          specialManouveure,
          radioStatus,
          flags,
          inputAisDataFile,
          year,
          month,
          day
        )
    }
  }

}

case class ShipPing(
    arkPosId: String,
    mmsi: String,
    acquisitionTime: Timestamp,
    longitude: Double,
    latitude: Double,
    vesselClass: String,
    messageTypeId: Integer,
    navigationStatus: String,
    rateOfTurn: String,
    speedOverGround: String,
    courseOverGround: String,
    trueHeading: String,
    altitude: String,
    specialManouveure: String,
    radioStatus: String,
    flags: String,
    inputAisDataFile: String,
    year: Integer,
    month: Integer,
    day: Integer
) {

  def toRow: Row = {
    Row(
      arkPosId,
      mmsi,
      acquisitionTime,
      longitude,
      latitude,
      vesselClass,
      messageTypeId,
      navigationStatus,
      rateOfTurn,
      speedOverGround,
      courseOverGround,
      trueHeading,
      altitude,
      specialManouveure,
      radioStatus,
      flags,
      inputAisDataFile,
      year,
      month,
      day
    )
  }
}
