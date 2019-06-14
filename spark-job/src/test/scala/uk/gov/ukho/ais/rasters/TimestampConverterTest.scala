package uk.gov.ukho.ais.rasters

import java.sql.Timestamp

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class TimestampConverterTest {

  @Test
  def whenValidDateSuppliedAndForStartOfDayThenTimestampReturnedAtMidnight()
    : Unit = {
    val date = "2019-02-10"

    val result =
      TimestampConverter.convertToTimestamp(date, forStartPeriod = true)

    assertThat(result).isEqualTo(Timestamp.valueOf("2019-02-10 00:00:00"))
  }

  @Test
  def whenValidDateSuppliedAndForEndOfDayThenTimestampReturnedAtEndOfDay()
    : Unit = {
    val date = "2019-02-10"

    val result =
      TimestampConverter.convertToTimestamp(date, forStartPeriod = false)

    assertThat(result).isEqualTo(Timestamp.valueOf("2019-02-11 00:00:00"))
  }
}
