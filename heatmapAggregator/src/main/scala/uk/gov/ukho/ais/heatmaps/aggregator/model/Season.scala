package uk.gov.ukho.ais.heatmaps.aggregator.model

object Season extends Enumeration {
  type Season = Value
  val Winter, Spring, Summer, Autumn = Value

  private final val MONTH_TO_SEASON_MAP: Map[Int, Season] = Map(
    11 -> Winter,
    12 -> Winter,
    1 -> Winter,
    2 -> Spring,
    3 -> Spring,
    4 -> Spring,
    5 -> Summer,
    6 -> Summer,
    7 -> Summer,
    8 -> Autumn,
    9 -> Autumn,
    10 -> Autumn
  )

  def monthToSeason(month: Int): Season = {
    MONTH_TO_SEASON_MAP(month)
  }

}
