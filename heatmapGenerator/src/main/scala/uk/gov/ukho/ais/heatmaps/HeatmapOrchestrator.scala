package uk.gov.ukho.ais.heatmaps

import com.amazonaws.services.s3.{AmazonS3, AmazonS3ClientBuilder}
import uk.gov.ukho.ais.heatmaps.processor.YearMonthFilter.Filter
import uk.gov.ukho.ais.heatmaps.processor.{RasterGenerator, Resampler}
import uk.gov.ukho.ais.heatmaps.repository.{
  AisRepository,
  FilterQuerySqlRepository,
  GeoTiffRepository
}
import javax.sql.DataSource

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
