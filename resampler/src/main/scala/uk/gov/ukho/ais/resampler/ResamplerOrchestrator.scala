package uk.gov.ukho.ais.resampler

import com.amazonaws.services.s3.AmazonS3
import javax.sql.DataSource
import uk.gov.ukho.ais.resampler.processor.Resampler
import uk.gov.ukho.ais.resampler.processor.YearMonthFilter.Filter
import uk.gov.ukho.ais.resampler.repository.AisRepository._
import uk.gov.ukho.ais.resampler.repository.CsvRepository
import uk.gov.ukho.ais.resampler.utility.TimeUtilities

object ResamplerOrchestrator {

  def orchestrateResampling()(implicit config: Config,
                              amazonS3: AmazonS3,
                              dataSource: DataSource): Unit = {

    println("querying for months contained in input AIS data files...")
    val modifiedMonths =
      dataSource.getDistinctYearAndMonthPairsForFiles(config.inputFiles)

    println(s"will resample ${modifiedMonths.size} month(s)")

    modifiedMonths.par.foreach {
      case (year: Int, month: Int) =>
        val monthOfPings =
          dataSource.getFilteredPingsByDate(year, month)

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
