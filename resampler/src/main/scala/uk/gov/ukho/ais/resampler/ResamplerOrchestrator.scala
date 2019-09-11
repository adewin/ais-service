package uk.gov.ukho.ais.resampler

import com.amazonaws.services.s3.AmazonS3
import javax.sql.DataSource
import uk.gov.ukho.ais.resampler.processor.Resampler
import uk.gov.ukho.ais.resampler.processor.YearMonthFilter.Filter
import uk.gov.ukho.ais.resampler.repository.{AisRepository, CsvRepository}
import uk.gov.ukho.ais.resampler.utility.TimeUtilities

object ResamplerOrchestrator {

  def orchestrateResampling(source: DataSource)(implicit config: Config,
                                                amazonS3: AmazonS3): Unit = {
    val aisRepository = new AisRepository(source)

    val modifiedMonths = config.inputFiles
      .flatMap { path =>
        println(s"querying for months contained in input AIS data file: $path...")
        aisRepository.getDistinctYearAndMonthPairsForFile(path)
      }

    println(s"will resample ${modifiedMonths.size} month(s)")

    modifiedMonths.foreach {
      case (year: Int, month: Int) =>
        val monthOfPings =
          aisRepository.getFilteredPingsByDate(year, month)

        println(s"resampling pings for year $year, month $month...")
        val resampledPings = Resampler
          .resamplePings(monthOfPings)
          .filterPingsByYearAndMonth(year, month)

        println(s"creating CSV for year $year, month $month...")
        CsvRepository.writePingsForMonth(year, month, resampledPings)
    }
  }

  def getMonthsToResampleFromModifiedMonths(
      list: Seq[(Int, Int)]): Set[(Int, Int)] = {
    list.flatMap {
      case (year: Int, month: Int) =>
        Seq(TimeUtilities.getPrevMonth(year, month),
            (year, month),
            TimeUtilities.getNextMonth(year, month))
    }.toSet
  }
}
