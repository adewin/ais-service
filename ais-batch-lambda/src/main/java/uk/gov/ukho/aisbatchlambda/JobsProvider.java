package uk.gov.ukho.aisbatchlambda;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class JobsProvider {

  private static final Double DEGREES_AT_EQUATOR_FOR_1KM_RESOLUTION = 0.008983031;
  private static final long DISTANCE_INTERPOLATION_THRESHOLD_30KM = 30L * 1000;
  private static final long DISTANCE_INTERPOLATION_THRESHOLD_100KM = 100L * 1000;
  private static final long ONE_HOUR_IN_MILLISECONDS = 60L * 60 * 1000;
  private static final long TIME_INTERPOLATION_THRESHOLD_6HR = 6L * ONE_HOUR_IN_MILLISECONDS;
  private static final long TIME_INTERPOLATION_THRESHOLD_18HR = 18L * ONE_HOUR_IN_MILLISECONDS;

  private static JobsProvider instance;

  private JobsProvider() {}

  public static JobsProvider getInstance() {
    if (instance == null) {
      instance = new JobsProvider();
    }

    return instance;
  }

  public List<Job> getJobs() {
    final List<Job> jobs =
        new ArrayList<>(
            Arrays.asList(
                new Job(
                    "seasonal-world-1k-30km-6hr-Autumn-Aug-2018-Oct-2018",
                    DEGREES_AT_EQUATOR_FOR_1KM_RESOLUTION,
                    DISTANCE_INTERPOLATION_THRESHOLD_30KM,
                    TIME_INTERPOLATION_THRESHOLD_6HR,
                    "2018-08-01",
                    "2018-10-31",
                    "/",
                    false,
                    false),
                new Job(
                    "seasonal-world-1k-30km-6hr-Winter-Nov-2018-Jan-2019",
                    DEGREES_AT_EQUATOR_FOR_1KM_RESOLUTION,
                    DISTANCE_INTERPOLATION_THRESHOLD_30KM,
                    TIME_INTERPOLATION_THRESHOLD_6HR,
                    "2018-11-01",
                    "2019-01-31",
                    "/",
                    false,
                    false),
                new Job(
                    "seasonal-world-1k-30km-6hr-Spring-Feb-2019-Apr-2019",
                    DEGREES_AT_EQUATOR_FOR_1KM_RESOLUTION,
                    DISTANCE_INTERPOLATION_THRESHOLD_30KM,
                    TIME_INTERPOLATION_THRESHOLD_6HR,
                    "2019-02-01",
                    "2019-04-30",
                    "/",
                    false,
                    false),
                new Job(
                    "annual-world-1k-30km-6hr-Jul-2018-May-2019",
                    DEGREES_AT_EQUATOR_FOR_1KM_RESOLUTION,
                    DISTANCE_INTERPOLATION_THRESHOLD_30KM,
                    TIME_INTERPOLATION_THRESHOLD_6HR,
                    "2018-07-01",
                    "2019-05-31",
                    "/",
                    false,
                    false),
                new Job(
                    "annual-world-1k-100km-18hr-Jul-2018-May-2019",
                    DEGREES_AT_EQUATOR_FOR_1KM_RESOLUTION,
                    DISTANCE_INTERPOLATION_THRESHOLD_100KM,
                    TIME_INTERPOLATION_THRESHOLD_18HR,
                    "2018-07-01",
                    "2019-05-31",
                    "/",
                    false,
                    false)));

    jobs.addAll(
        createDraughtJobs(
            "30km-6hr", DISTANCE_INTERPOLATION_THRESHOLD_30KM, TIME_INTERPOLATION_THRESHOLD_6HR));

    jobs.addAll(
        createDraughtJobs(
            "100km-18hr",
            DISTANCE_INTERPOLATION_THRESHOLD_100KM,
            TIME_INTERPOLATION_THRESHOLD_18HR));

    jobs.addAll(createMonthlyJobs());

    return jobs;
  }

  private List<Job> createDraughtJobs(
      final String interpolationPrefix,
      final long distanceInterpolationThreshold,
      final long timeInterpolationThreshold) {

    final List<Job> jobs = new ArrayList<>();

    jobs.add(
        new Job(
            "annual-world-1k-unknowndraught-" + interpolationPrefix + "-Jul-2018-May-2019",
            DEGREES_AT_EQUATOR_FOR_1KM_RESOLUTION,
            distanceInterpolationThreshold,
            timeInterpolationThreshold,
            "2018-07-01",
            "2019-05-31",
            "/",
            true,
            true,
            "unknown"));

    int draughtIndex = 0;

    for (int bucket = 1; bucket <= 3; bucket++) {
      jobs.add(
          new Job(
              "annual-world-1k-cat1draught"
                  + bucket
                  + "-"
                  + interpolationPrefix
                  + "-Jul-2018-May-2019",
              DEGREES_AT_EQUATOR_FOR_1KM_RESOLUTION,
              distanceInterpolationThreshold,
              timeInterpolationThreshold,
              "2018-07-01",
              "2019-05-31",
              "/",
              true,
              true,
              String.valueOf(draughtIndex)));
      draughtIndex++;
    }

    for (int bucket = 1; bucket <= 5; bucket++) {
      jobs.add(
          new Job(
              "annual-world-1k-cat2draught"
                  + bucket
                  + "-"
                  + interpolationPrefix
                  + "-Jul-2018-May-2019",
              DEGREES_AT_EQUATOR_FOR_1KM_RESOLUTION,
              distanceInterpolationThreshold,
              timeInterpolationThreshold,
              "2018-07-01",
              "2019-05-31",
              "/",
              true,
              true,
              String.valueOf(draughtIndex)));
      draughtIndex++;
    }

    return jobs;
  }

  private List<Job> createMonthlyJobs() {
    return Arrays.asList(
        createMonthlyJob(2018, 7),
        createMonthlyJob(2018, 8),
        createMonthlyJob(2018, 9),
        createMonthlyJob(2018, 10),
        createMonthlyJob(2018, 11),
        createMonthlyJob(2018, 12),
        createMonthlyJob(2019, 1),
        createMonthlyJob(2019, 2),
        createMonthlyJob(2019, 3),
        createMonthlyJob(2019, 4),
        createMonthlyJob(2019, 5));
  }

  private Job createMonthlyJob(final int year, final int month) {
    final LocalDate startOfMonth = LocalDate.of(year, month, 1);
    final LocalDate endOfMonth = startOfMonth.plusMonths(1).minusDays(1);
    return new Job(
        "monthly-world-1k-30km-6hr-"
            + startOfMonth.getMonth().getDisplayName(TextStyle.SHORT, Locale.getDefault())
            + "-"
            + year,
        DEGREES_AT_EQUATOR_FOR_1KM_RESOLUTION,
        DISTANCE_INTERPOLATION_THRESHOLD_30KM,
        TIME_INTERPOLATION_THRESHOLD_6HR,
        startOfMonth.format(DateTimeFormatter.ISO_DATE),
        endOfMonth.format(DateTimeFormatter.ISO_DATE),
        "/",
        false,
        false);
  }
}
