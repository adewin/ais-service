package uk.gov.ukho.ais.heatmaps

import uk.gov.ukho.ais.heatmaps.service.AthenaDataSourceProvider
import javax.sql.DataSource

object Main {

  def main(args: Array[String]): Unit = {
    implicit val config: Config = ConfigParser.parse(args)

    val source = AthenaDataSourceProvider.dataSource

    HeatmapOrchestrator.orchestrateHeatmapGeneration(source)
  }
}
