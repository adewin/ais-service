package uk.gov.ukho.aisbatchlambda;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import uk.gov.ukho.ais.emrjobrunner.model.AbstractJob;

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

  public List<AbstractJob> getJobs() {
    final List<AbstractJob> batchJobs =
        new ArrayList<>(
            Arrays.asList(
                new HeatmapJob(
                    "seasonal-world-1k-30km-6hr-Autumn-Aug-2018-Oct-2018",
                    DEGREES_AT_EQUATOR_FOR_1KM_RESOLUTION,
                    DISTANCE_INTERPOLATION_THRESHOLD_30KM,
                    TIME_INTERPOLATION_THRESHOLD_6HR,
                    "2018-08-01",
                    "2018-10-31",
                    "/",
                    false,
                    false),
                new HeatmapJob(
                    "seasonal-world-1k-30km-6hr-Winter-Nov-2018-Jan-2019",
                    DEGREES_AT_EQUATOR_FOR_1KM_RESOLUTION,
                    DISTANCE_INTERPOLATION_THRESHOLD_30KM,
                    TIME_INTERPOLATION_THRESHOLD_6HR,
                    "2018-11-01",
                    "2019-01-31",
                    "/",
                    false,
                    false),
                new HeatmapJob(
                    "seasonal-world-1k-30km-6hr-Spring-Feb-2019-Apr-2019",
                    DEGREES_AT_EQUATOR_FOR_1KM_RESOLUTION,
                    DISTANCE_INTERPOLATION_THRESHOLD_30KM,
                    TIME_INTERPOLATION_THRESHOLD_6HR,
                    "2019-02-01",
                    "2019-04-30",
                    "/",
                    false,
                    false),
                new HeatmapJob(
                    "seasonal-world-1k-100km-18hr-Autumn-Aug-2018-Oct-2018",
                    DEGREES_AT_EQUATOR_FOR_1KM_RESOLUTION,
                    DISTANCE_INTERPOLATION_THRESHOLD_100KM,
                    TIME_INTERPOLATION_THRESHOLD_18HR,
                    "2018-08-01",
                    "2018-10-31",
                    "/",
                    false,
                    true),
                new HeatmapJob(
                    "seasonal-world-1k-100km-18hr-Winter-Nov-2018-Jan-2019",
                    DEGREES_AT_EQUATOR_FOR_1KM_RESOLUTION,
                    DISTANCE_INTERPOLATION_THRESHOLD_100KM,
                    TIME_INTERPOLATION_THRESHOLD_18HR,
                    "2018-11-01",
                    "2019-01-31",
                    "/",
                    false,
                    true),
                new HeatmapJob(
                    "seasonal-world-1k-100km-18hr-Spring-Feb-2019-Apr-2019",
                    DEGREES_AT_EQUATOR_FOR_1KM_RESOLUTION,
                    DISTANCE_INTERPOLATION_THRESHOLD_100KM,
                    TIME_INTERPOLATION_THRESHOLD_18HR,
                    "2019-02-01",
                    "2019-04-30",
                    "/",
                    false,
                    true),
                new HeatmapJob(
                    "annual-world-1k-30km-6hr-Jul-2018-May-2019",
                    DEGREES_AT_EQUATOR_FOR_1KM_RESOLUTION,
                    DISTANCE_INTERPOLATION_THRESHOLD_30KM,
                    TIME_INTERPOLATION_THRESHOLD_6HR,
                    "2018-07-01",
                    "2019-05-31",
                    "/",
                    false,
                    false),
                new HeatmapJob(
                    "annual-world-1k-100km-18hr-Jul-2018-May-2019",
                    DEGREES_AT_EQUATOR_FOR_1KM_RESOLUTION,
                    DISTANCE_INTERPOLATION_THRESHOLD_100KM,
                    TIME_INTERPOLATION_THRESHOLD_18HR,
                    "2018-07-01",
                    "2019-05-31",
                    "/",
                    false,
                    false)));

    batchJobs.addAll(
        createDraughtJobs(
            "30km-6hr", DISTANCE_INTERPOLATION_THRESHOLD_30KM, TIME_INTERPOLATION_THRESHOLD_6HR));

    batchJobs.addAll(
        createDraughtJobs(
            "100km-18hr",
            DISTANCE_INTERPOLATION_THRESHOLD_100KM,
            TIME_INTERPOLATION_THRESHOLD_18HR));

    batchJobs.addAll(createMonthlyJobs());

    return batchJobs;
  }

  private List<HeatmapJob> createDraughtJobs(
      final String interpolationPrefix,
      final long distanceInterpolationThreshold,
      final long timeInterpolationThreshold) {

    final List<HeatmapJob> heatmapJobs = new ArrayList<>();

    heatmapJobs.add(
        new HeatmapJob(
            "annual-world-1k-unknowndraught-" + interpolationPrefix + "-Jul-2018-May-2019",
            DEGREES_AT_EQUATOR_FOR_1KM_RESOLUTION,
            distanceInterpolationThreshold,
            timeInterpolationThreshold,
            "2018-07-01",
            "2019-05-31",
            "/",
            true,
            false,
            "unknown"));

    int draughtIndex = 0;

    for (int bucket = 1; bucket <= 3; bucket++) {
      heatmapJobs.add(
          new HeatmapJob(
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
              false,
              String.valueOf(draughtIndex)));
      draughtIndex++;
    }

    for (int bucket = 1; bucket <= 5; bucket++) {
      heatmapJobs.add(
          new HeatmapJob(
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
              false,
              String.valueOf(draughtIndex)));
      draughtIndex++;
    }

    return heatmapJobs;
  }

  private List<HeatmapJob> createMonthlyJobs() {
    return Arrays.asList(
        create30Km6HrMonthlyJob(2018, 7, false),
        create30Km6HrMonthlyJob(2018, 8, false),
        create30Km6HrMonthlyJob(2018, 9, false),
        create30Km6HrMonthlyJob(2018, 10, false),
        create30Km6HrMonthlyJob(2018, 11, false),
        create30Km6HrMonthlyJob(2018, 12, false),
        create30Km6HrMonthlyJob(2019, 1, false),
        create30Km6HrMonthlyJob(2019, 2, false),
        create30Km6HrMonthlyJob(2019, 3, false),
        create30Km6HrMonthlyJob(2019, 4, false),
        create30Km6HrMonthlyJob(2019, 5, false),
        create30Km6HrMonthlyJob(2019, 6, true),
        create100Km18HrMonthlyJob(2018, 7, true),
        create100Km18HrMonthlyJob(2018, 8, true),
        create100Km18HrMonthlyJob(2018, 9, true),
        create100Km18HrMonthlyJob(2018, 10, true),
        create100Km18HrMonthlyJob(2018, 11, true),
        create100Km18HrMonthlyJob(2018, 12, true),
        create100Km18HrMonthlyJob(2019, 1, true),
        create100Km18HrMonthlyJob(2019, 2, true),
        create100Km18HrMonthlyJob(2019, 3, true),
        create100Km18HrMonthlyJob(2019, 4, true),
        create100Km18HrMonthlyJob(2019, 5, true),
        create100Km18HrMonthlyJob(2019, 6, true));
  }

  private HeatmapJob create30Km6HrMonthlyJob(
      final int year, final int month, final boolean enabled) {
    final LocalDate startOfMonth = LocalDate.of(year, month, 1);
    final LocalDate endOfMonth = startOfMonth.plusMonths(1).minusDays(1);
    return new HeatmapJob(
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
        enabled);
  }

  private HeatmapJob create100Km18HrMonthlyJob(
      final int year, final int month, final boolean enabled) {
    final LocalDate startOfMonth = LocalDate.of(year, month, 1);
    final LocalDate endOfMonth = startOfMonth.plusMonths(1).minusDays(1);
    return new HeatmapJob(
        "monthly-world-1k-100km-18hr-"
            + startOfMonth.getMonth().getDisplayName(TextStyle.SHORT, Locale.getDefault())
            + "-"
            + year,
        DEGREES_AT_EQUATOR_FOR_1KM_RESOLUTION,
        DISTANCE_INTERPOLATION_THRESHOLD_100KM,
        TIME_INTERPOLATION_THRESHOLD_18HR,
        startOfMonth.format(DateTimeFormatter.ISO_DATE),
        endOfMonth.format(DateTimeFormatter.ISO_DATE),
        "/",
        false,
        enabled);
  }
}
