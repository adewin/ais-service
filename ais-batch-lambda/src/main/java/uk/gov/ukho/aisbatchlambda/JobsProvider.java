package uk.gov.ukho.aisbatchlambda;

import java.util.Arrays;
import java.util.List;

public class JobsProvider {

  private static final Double DEGREES_AT_EQUATOR_FOR_1KM_RESOLUTION = 0.008983031;

  private static JobsProvider instance;

  private JobsProvider() {}

  public static JobsProvider getInstance() {
    if (instance == null) {
      instance = new JobsProvider();
    }

    return instance;
  }

  public List<Job> getJobs() {
    return Arrays.asList(
        new Job(DEGREES_AT_EQUATOR_FOR_1KM_RESOLUTION, "world-1k", 30000L, 6L * 60 * 60 * 1000));
  }
}
