package uk.gov.ukho.ais.rasters

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class SessionTest {

  @Test
  def whenSparkSessionCalledAndThereIsSparkSessionPresentThenSparkSessionReturned()
    : Unit = {
    Session.init(true)
    val session = Session.sparkSession
    assertThat(session).isNotNull
  }

}
