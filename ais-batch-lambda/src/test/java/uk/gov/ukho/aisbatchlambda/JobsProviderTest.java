package uk.gov.ukho.aisbatchlambda;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import org.junit.Test;

public class JobsProviderTest {
  private static final Double DEGREES_AT_EQUATOR_FOR_1KM_RESOLUTION = 0.008983031;

  @Test
  public void whenGetJobsThenCorrectJobsReturned() {
    assertThat(JobsProvider.getInstance().getJobs())
        .as("Jobs returned are correct")
        .extracting("resolution", "prefix")
        .containsExactlyInAnyOrder(tuple(DEGREES_AT_EQUATOR_FOR_1KM_RESOLUTION, "world-1k"));
  }
}
