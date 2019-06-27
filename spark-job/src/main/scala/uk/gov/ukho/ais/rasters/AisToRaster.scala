package uk.gov.ukho.ais.rasters

import uk.gov.ukho.ais.rasters.AisDataFilters.RDDFilters
import uk.gov.ukho.ais.rasters.AisRasterMapper.ShipPingRasterMapper
import uk.gov.ukho.ais.rasters.Resampler.RDDResampler

object AisToRaster {

  def main(args: Array[String]): Unit = {
    Session.init()

    val config: Config = ConfigParser.parse(args)

    val raster = DataLoader
      .loadAisData(config)
      .filterShipPings(config)
      .resample(config)
      .mapToRaster(config)

    FileCreator.createOutputFiles(raster, config)
  }

}
