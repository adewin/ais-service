package uk.gov.ukho.ais

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class SessionTest {

  @Test
  def whenInitialisedAsTestAndGetSessionCalledThenSessionIsCreatedAndHasCorrectNameAndMasterIsLocal()
    : Unit = {
    Session.init("appname", isTestSession = true)

    assertThat(Session.sparkSession.sparkContext.appName)
      .isEqualTo("Test session for: appname")
    assertThat(Session.sparkSession.sparkContext.master).isEqualTo("local[*]")
  }
}
