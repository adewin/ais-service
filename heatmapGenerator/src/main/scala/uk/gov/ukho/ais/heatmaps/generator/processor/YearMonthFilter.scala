package uk.gov.ukho.ais.heatmaps.generator.processor

import uk.gov.ukho.ais.heatmaps.generator.model.Ping

object YearMonthFilter {

  implicit class Filter(pings: Iterator[Ping]) {

    def filterPingsByYearAndMonth(year: Int, month: Int): Iterator[Ping] = {
      pings.filter(
        ping =>
          ping.acquisitionTime.toLocalDateTime.getYear == year &&
            ping.acquisitionTime.toLocalDateTime.getMonthValue == month)
    }
  }

}
