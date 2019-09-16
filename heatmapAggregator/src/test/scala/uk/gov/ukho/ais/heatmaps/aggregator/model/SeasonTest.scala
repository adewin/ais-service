package uk.gov.ukho.ais.heatmaps.aggregator.model

import org.assertj.core.api.SoftAssertions
import org.junit.Test

class SeasonTest {

  @Test
  def whenGettingSeasonForMonthThenSeasonsAreCorrect(): Unit =
    SoftAssertions.assertSoftly { softly =>
      softly.assertThat(Season.monthToSeason(1)).isEqualTo(Season.Winter)
      softly.assertThat(Season.monthToSeason(2)).isEqualTo(Season.Spring)
      softly.assertThat(Season.monthToSeason(3)).isEqualTo(Season.Spring)
      softly.assertThat(Season.monthToSeason(4)).isEqualTo(Season.Spring)
      softly.assertThat(Season.monthToSeason(5)).isEqualTo(Season.Summer)
      softly.assertThat(Season.monthToSeason(6)).isEqualTo(Season.Summer)
      softly.assertThat(Season.monthToSeason(7)).isEqualTo(Season.Summer)
      softly.assertThat(Season.monthToSeason(8)).isEqualTo(Season.Autumn)
      softly.assertThat(Season.monthToSeason(9)).isEqualTo(Season.Autumn)
      softly.assertThat(Season.monthToSeason(10)).isEqualTo(Season.Autumn)
      softly.assertThat(Season.monthToSeason(11)).isEqualTo(Season.Winter)
      softly.assertThat(Season.monthToSeason(12)).isEqualTo(Season.Winter)
    }
}
