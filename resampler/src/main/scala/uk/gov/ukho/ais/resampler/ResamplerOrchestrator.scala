package uk.gov.ukho.ais.resampler

import com.amazonaws.services.s3.{AmazonS3, AmazonS3ClientBuilder}
import uk.gov.ukho.ais.resampler.processor.YearMonthFilter.Filter
import uk.gov.ukho.ais.resampler.processor.{RasterGenerator, Resampler}
import uk.gov.ukho.ais.resampler.repository.AisRepository
import javax.sql.DataSource
import uk.gov.ukho.ais.resampler.utility.TimeUtilities

object ResamplerOrchestrator {

  def orchestrateResampling(source: DataSource)
                           (implicit config: Config, amazonS3: AmazonS3): Unit = {

    val aisRepository = new AisRepository(source)

    val modifiedMonths = config.inputFiles
      .flatMap { path => aisRepository.getDistinctYearAndMonthPairsForFile(path) }

    getMonthsToUpdateFromModifiedMonths(modifiedMonths).foreach {
      case (year: Int, month: Int) =>
        val monthOfPings =
          aisRepository.getFilteredPingsByDate(year, month)

        println("Resampling pings...")
        val resampledPings = Resampler
          .resamplePings(monthOfPings)
          .filterPingsByYearAndMonth(year, month)

        val rasterMap = RasterGenerator.mapToRaster(resampledPings)

        println("Creating CSV...")
        // TODO: create CSVs
    }
  }

  // TODO: better name
  def getMonthsToUpdateFromModifiedMonths(list: Seq[(Int, Int)]): Set[(Int, Int)] = {
    list
      .flatMap {
        case (year: Int, month: Int) =>
          Seq(TimeUtilities.getPrevMonth(year, month), (year, month), TimeUtilities.getNextMonth(year, month))
      }
      .toSet
  }
}
