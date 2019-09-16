package uk.gov.ukho.ais.heatmaps.generator

import com.amazonaws.services.s3.AmazonS3
import javax.sql.DataSource
import uk.gov.ukho.ais.heatmaps.generator.processor.YearMonthFilter.Filter
import uk.gov.ukho.ais.heatmaps.generator.processor.{RasterGenerator, Resampler}
import uk.gov.ukho.ais.heatmaps.generator.repository.{
  AisRepository,
  FilterQuerySqlRepository,
  GeoTiffRepository
}

object HeatmapOrchestrator {

  def orchestrateHeatmapGeneration(
      source: DataSource)(implicit config: Config, amazonS3: AmazonS3): Unit = {

    val filterQuerySqlRepository = new FilterQuerySqlRepository()
    val aisRepository = new AisRepository(source)

    val query = filterQuerySqlRepository.retrieveFilterQuery
    val monthOfPings =
      aisRepository.getFilteredPingsByDate(query, config.year, config.month)

    println("Resampling pings...")
    val resampledPings = Resampler
      .resamplePings(monthOfPings)
      .filterPingsByYearAndMonth(config.year, config.month)

    val rasterMap = RasterGenerator.mapToRaster(resampledPings)

    println("Creating GeoTIFF and PNG...")
    GeoTiffRepository.createOutputFiles(rasterMap)
  }

}
