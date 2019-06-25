package uk.gov.ukho.ais.rasters

import uk.gov.ukho.ais.rasters.AisDataFilters.RDDFilters
import uk.gov.ukho.ais.rasters.AisRasterMapper.ShipPingRasterMapper
import uk.gov.ukho.ais.rasters.Resampler.RDDResampler

object AisToRaster {

  def main(args: Array[String]): Unit = {
    Session.init()

    ConfigParser.parse(args)

    orchestrateHeatmapGeneration()
  }

  def orchestrateHeatmapGeneration(): Unit = {
    val raster = DataLoader
      .loadAisData()
      .filterShipPings()
      .resample()
      .mapToRaster()

    FileCreator.createOutputFiles(raster)
  }

}
