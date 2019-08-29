package uk.gov.ukho.ais.heatmaps

import uk.gov.ukho.ais.heatmaps.processor.YearMonthFilter.Filter
import uk.gov.ukho.ais.heatmaps.processor.{RasterGenerator, Resampler}
import uk.gov.ukho.ais.heatmaps.repository.{AisRepository, GeoTiffRepository}
import javax.sql.DataSource

object HeatmapOrchestrator {

  def orchestrateHeatmapGeneration(source: DataSource)(
      implicit config: Config): Unit = {
    val aisRepository = new AisRepository(source)

    val monthOfPings =
      aisRepository.getPingsByDate(config.year, config.month)

    println("Resampling pings...")
    val resampledPings = Resampler
      .resamplePings(monthOfPings)
      .filterPingsByYearAndMonth(config.year, config.month)

    val rasterMap = RasterGenerator.mapToRaster(resampledPings)

    println("Creating GeoTIFF and PNG...")
    GeoTiffRepository.createOutputFiles(rasterMap)
  }

}
