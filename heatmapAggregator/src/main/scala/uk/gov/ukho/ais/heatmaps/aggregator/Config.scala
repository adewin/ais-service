package uk.gov.ukho.ais.heatmaps.aggregator

case class Config(heatmapsDirectory: String)

object Config {
  val default: Config = Config(
    heatmapsDirectory = ""
  )
}
