package uk.gov.ukho.aisbatchlambda;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import org.junit.Test;

public class JobsProviderTest {
  private static final Double DEGREES_AT_EQUATOR_FOR_1KM_RESOLUTION = 0.008983031;
  private static final long DISTANCE_INTERPOLATION_THRESHOLD_30KM = 30L * 1000;
  private static final long DISTANCE_INTERPOLATION_THRESHOLD_100KM = 100L * 1000;
  private static final long DISTANCE_INTERPOLATION_THRESHOLD_500KM = 500L * 1000;
  private static final long ONE_HOUR_IN_MILLISECONDS = 60L * 60 * 1000;
  private static final long TIME_INTERPOLATION_THRESHOLD_6HR = 6L * ONE_HOUR_IN_MILLISECONDS;
  private static final long TIME_INTERPOLATION_THRESHOLD_18HR = 18L * ONE_HOUR_IN_MILLISECONDS;
  private static final long TIME_INTERPOLATION_THRESHOLD_48HR = 48L * ONE_HOUR_IN_MILLISECONDS;

  @Test
  public void whenGetJobsThenCorrectJobsReturned() {
    assertThat(JobsProvider.getInstance().getJobs())
        .as("Jobs returned are correct")
        .extracting(
            "resolution",
            "prefix",
            "distanceInterpolationThreshold",
            "timeInterpolationThreshold",
            "startPeriod",
            "endPeriod")
        .containsExactlyInAnyOrder(
            tuple(
                DEGREES_AT_EQUATOR_FOR_1KM_RESOLUTION,
                "monthly-world-1k-30km-6hr-Jul-18",
                DISTANCE_INTERPOLATION_THRESHOLD_30KM,
                TIME_INTERPOLATION_THRESHOLD_6HR,
                "2018-07-01",
                "2018-07-31"),
            tuple(
                DEGREES_AT_EQUATOR_FOR_1KM_RESOLUTION,
                "monthly-world-1k-30km-6hr-Aug-18",
                DISTANCE_INTERPOLATION_THRESHOLD_30KM,
                TIME_INTERPOLATION_THRESHOLD_6HR,
                "2018-08-01",
                "2018-08-31"),
            tuple(
                DEGREES_AT_EQUATOR_FOR_1KM_RESOLUTION,
                "monthly-world-1k-30km-6hr-Sep-18",
                DISTANCE_INTERPOLATION_THRESHOLD_30KM,
                TIME_INTERPOLATION_THRESHOLD_6HR,
                "2018-09-01",
                "2018-09-30"),
            tuple(
                DEGREES_AT_EQUATOR_FOR_1KM_RESOLUTION,
                "monthly-world-1k-30km-6hr-Oct-18",
                DISTANCE_INTERPOLATION_THRESHOLD_30KM,
                TIME_INTERPOLATION_THRESHOLD_6HR,
                "2018-10-01",
                "2018-10-31"),
            tuple(
                DEGREES_AT_EQUATOR_FOR_1KM_RESOLUTION,
                "monthly-world-1k-30km-6hr-Nov-18",
                DISTANCE_INTERPOLATION_THRESHOLD_30KM,
                TIME_INTERPOLATION_THRESHOLD_6HR,
                "2018-11-01",
                "2018-11-30"),
            tuple(
                DEGREES_AT_EQUATOR_FOR_1KM_RESOLUTION,
                "monthly-world-1k-30km-6hr-Dec-18",
                DISTANCE_INTERPOLATION_THRESHOLD_30KM,
                TIME_INTERPOLATION_THRESHOLD_6HR,
                "2018-12-01",
                "2018-12-31"),
            tuple(
                DEGREES_AT_EQUATOR_FOR_1KM_RESOLUTION,
                "monthly-world-1k-30km-6hr-Jan-19",
                DISTANCE_INTERPOLATION_THRESHOLD_30KM,
                TIME_INTERPOLATION_THRESHOLD_6HR,
                "2019-01-01",
                "2019-01-31"),
            tuple(
                DEGREES_AT_EQUATOR_FOR_1KM_RESOLUTION,
                "monthly-world-1k-30km-6hr-Feb-19",
                DISTANCE_INTERPOLATION_THRESHOLD_30KM,
                TIME_INTERPOLATION_THRESHOLD_6HR,
                "2019-02-01",
                "2019-02-28"),
            tuple(
                DEGREES_AT_EQUATOR_FOR_1KM_RESOLUTION,
                "monthly-world-1k-30km-6hr-Mar-19",
                DISTANCE_INTERPOLATION_THRESHOLD_30KM,
                TIME_INTERPOLATION_THRESHOLD_6HR,
                "2019-03-01",
                "2019-03-31"),
            tuple(
                DEGREES_AT_EQUATOR_FOR_1KM_RESOLUTION,
                "monthly-world-1k-30km-6hr-Apr-19",
                DISTANCE_INTERPOLATION_THRESHOLD_30KM,
                TIME_INTERPOLATION_THRESHOLD_6HR,
                "2019-04-01",
                "2019-04-30"),
            tuple(
                DEGREES_AT_EQUATOR_FOR_1KM_RESOLUTION,
                "monthly-world-1k-30km-6hr-May-19",
                DISTANCE_INTERPOLATION_THRESHOLD_30KM,
                TIME_INTERPOLATION_THRESHOLD_6HR,
                "2019-05-01",
                "2019-05-31"),
            tuple(
                DEGREES_AT_EQUATOR_FOR_1KM_RESOLUTION,
                "seasonal-world-1k-30km-6hr-Autumn-Aug-18-Oct-18",
                DISTANCE_INTERPOLATION_THRESHOLD_30KM,
                TIME_INTERPOLATION_THRESHOLD_6HR,
                "2018-08-01",
                "2018-10-31"),
            tuple(
                DEGREES_AT_EQUATOR_FOR_1KM_RESOLUTION,
                "seasonal-world-1k-30km-6hr-Winter-Nov-18-Jan-19",
                DISTANCE_INTERPOLATION_THRESHOLD_30KM,
                TIME_INTERPOLATION_THRESHOLD_6HR,
                "2018-11-01",
                "2019-01-31"),
            tuple(
                DEGREES_AT_EQUATOR_FOR_1KM_RESOLUTION,
                "seasonal-world-1k-30km-6hr-Spring-Feb-19-Apr-19",
                DISTANCE_INTERPOLATION_THRESHOLD_30KM,
                TIME_INTERPOLATION_THRESHOLD_6HR,
                "2019-02-01",
                "2019-04-30"),
            tuple(
                DEGREES_AT_EQUATOR_FOR_1KM_RESOLUTION,
                "annual-world-1k-30km-6hr-Jul-18-May-19",
                DISTANCE_INTERPOLATION_THRESHOLD_30KM,
                TIME_INTERPOLATION_THRESHOLD_6HR,
                "2018-07-01",
                "2019-05-31"),
            tuple(
                DEGREES_AT_EQUATOR_FOR_1KM_RESOLUTION,
                "annual-world-1k-100km-18hr-Jul-18-May-19",
                DISTANCE_INTERPOLATION_THRESHOLD_100KM,
                TIME_INTERPOLATION_THRESHOLD_18HR,
                "2018-07-01",
                "2019-05-31"),
            tuple(
                DEGREES_AT_EQUATOR_FOR_1KM_RESOLUTION,
                "annual-world-1k-500km-48hr-Jul-18-May-19",
                DISTANCE_INTERPOLATION_THRESHOLD_500KM,
                TIME_INTERPOLATION_THRESHOLD_48HR,
                "2018-07-01",
                "2019-05-31"));
  }
}
