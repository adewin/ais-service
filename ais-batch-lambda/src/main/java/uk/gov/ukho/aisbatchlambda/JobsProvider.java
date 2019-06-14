package uk.gov.ukho.aisbatchlambda;

import java.util.Arrays;
import java.util.List;

public class JobsProvider {

  private static final Double DEGREES_AT_EQUATOR_FOR_1KM_RESOLUTION = 0.008983031;
  private static final long DISTANCE_INTERPOLATION_THRESHOLD = 30000L;
  private static final long TIME_INTERPOLATION_THRESHOLD = 6L * 60 * 60 * 1000;

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
        new Job(
            DEGREES_AT_EQUATOR_FOR_1KM_RESOLUTION,
            "annual-world-1k-Jun-18-May-19",
            DISTANCE_INTERPOLATION_THRESHOLD,
            TIME_INTERPOLATION_THRESHOLD,
            "2018-06-01",
            "2019-05-31"),
        new Job(
            DEGREES_AT_EQUATOR_FOR_1KM_RESOLUTION,
            "monthly-world-1k-Jun-18",
            DISTANCE_INTERPOLATION_THRESHOLD,
            TIME_INTERPOLATION_THRESHOLD,
            "2018-06-01",
            "2018-06-30"),
        new Job(
            DEGREES_AT_EQUATOR_FOR_1KM_RESOLUTION,
            "monthly-world-1k-Jul-18",
            DISTANCE_INTERPOLATION_THRESHOLD,
            TIME_INTERPOLATION_THRESHOLD,
            "2018-07-01",
            "2018-07-31"),
        new Job(
            DEGREES_AT_EQUATOR_FOR_1KM_RESOLUTION,
            "monthly-world-1k-Aug-18",
            DISTANCE_INTERPOLATION_THRESHOLD,
            TIME_INTERPOLATION_THRESHOLD,
            "2018-08-01",
            "2018-08-31"),
        new Job(
            DEGREES_AT_EQUATOR_FOR_1KM_RESOLUTION,
            "monthly-world-1k-Sep-18",
            DISTANCE_INTERPOLATION_THRESHOLD,
            TIME_INTERPOLATION_THRESHOLD,
            "2018-09-01",
            "2018-09-30"),
        new Job(
            DEGREES_AT_EQUATOR_FOR_1KM_RESOLUTION,
            "monthly-world-1k-Oct-18",
            DISTANCE_INTERPOLATION_THRESHOLD,
            TIME_INTERPOLATION_THRESHOLD,
            "2018-10-01",
            "2018-10-31"),
        new Job(
            DEGREES_AT_EQUATOR_FOR_1KM_RESOLUTION,
            "monthly-world-1k-Nov-18",
            DISTANCE_INTERPOLATION_THRESHOLD,
            TIME_INTERPOLATION_THRESHOLD,
            "2018-11-01",
            "2018-11-30"),
        new Job(
            DEGREES_AT_EQUATOR_FOR_1KM_RESOLUTION,
            "monthly-world-1k-Dec-18",
            DISTANCE_INTERPOLATION_THRESHOLD,
            TIME_INTERPOLATION_THRESHOLD,
            "2018-12-01",
            "2018-12-31"),
        new Job(
            DEGREES_AT_EQUATOR_FOR_1KM_RESOLUTION,
            "monthly-world-1k-Jan-19",
            DISTANCE_INTERPOLATION_THRESHOLD,
            TIME_INTERPOLATION_THRESHOLD,
            "2019-01-01",
            "2019-01-31"),
        new Job(
            DEGREES_AT_EQUATOR_FOR_1KM_RESOLUTION,
            "monthly-world-1k-Feb-19",
            DISTANCE_INTERPOLATION_THRESHOLD,
            TIME_INTERPOLATION_THRESHOLD,
            "2019-02-01",
            "2019-02-28"),
        new Job(
            DEGREES_AT_EQUATOR_FOR_1KM_RESOLUTION,
            "monthly-world-1k-Mar-19",
            DISTANCE_INTERPOLATION_THRESHOLD,
            TIME_INTERPOLATION_THRESHOLD,
            "2019-03-01",
            "2019-03-31"),
        new Job(
            DEGREES_AT_EQUATOR_FOR_1KM_RESOLUTION,
            "monthly-world-1k-Apr-19",
            DISTANCE_INTERPOLATION_THRESHOLD,
            TIME_INTERPOLATION_THRESHOLD,
            "2019-04-01",
            "2019-04-30"),
        new Job(
            DEGREES_AT_EQUATOR_FOR_1KM_RESOLUTION,
            "monthly-world-1k-May-19",
            DISTANCE_INTERPOLATION_THRESHOLD,
            TIME_INTERPOLATION_THRESHOLD,
            "2019-05-01",
            "2019-05-31"),
        new Job(
            DEGREES_AT_EQUATOR_FOR_1KM_RESOLUTION,
            "seasonal-world-1k-Summer-May-18-Jul-18",
            DISTANCE_INTERPOLATION_THRESHOLD,
            TIME_INTERPOLATION_THRESHOLD,
            "2018-05-01",
            "2018-07-31"),
        new Job(
            DEGREES_AT_EQUATOR_FOR_1KM_RESOLUTION,
            "seasonal-world-1k-Autumn-Aug-18-Oct-18",
            DISTANCE_INTERPOLATION_THRESHOLD,
            TIME_INTERPOLATION_THRESHOLD,
            "2018-08-01",
            "2018-10-31"),
        new Job(
            DEGREES_AT_EQUATOR_FOR_1KM_RESOLUTION,
            "seasonal-world-1k-Winter-Nov-18-Jan-19",
            DISTANCE_INTERPOLATION_THRESHOLD,
            TIME_INTERPOLATION_THRESHOLD,
            "2018-11-01",
            "2019-01-31"),
        new Job(
            DEGREES_AT_EQUATOR_FOR_1KM_RESOLUTION,
            "seasonal-world-1k-Spring-Feb-19-Apr-19",
            DISTANCE_INTERPOLATION_THRESHOLD,
            TIME_INTERPOLATION_THRESHOLD,
            "2019-02-01",
            "2019-04-30"));
  }
}
