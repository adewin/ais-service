package uk.gov.ukho.ais.triggerresamplelambda.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import uk.gov.ukho.ais.emrjobrunner.model.AbstractJob;
import uk.gov.ukho.ais.triggerresamplelambda.configuration.ResampleLambdaConfiguration;

public class ResampleJobsSupplier implements Supplier<List<AbstractJob>> {

  private static final long DISTANCE_INTERPOLATION_THRESHOLD_30KM = 30L * 1000;
  private static final long DISTANCE_INTERPOLATION_THRESHOLD_100KM = 100L * 1000;
  private static final long ONE_HOUR_IN_MILLISECONDS = 60L * 60 * 1000;
  private static final long TIME_INTERPOLATION_THRESHOLD_6HR = 6L * ONE_HOUR_IN_MILLISECONDS;
  private static final long TIME_INTERPOLATION_THRESHOLD_18HR = 18L * ONE_HOUR_IN_MILLISECONDS;

  private final ResampleLambdaConfiguration resampleLambdaConfiguration;

  public ResampleJobsSupplier(final ResampleLambdaConfiguration resampleLambdaConfiguration) {
    this.resampleLambdaConfiguration = resampleLambdaConfiguration;
  }

  @Override
  public List<AbstractJob> get() {
    final ResampleJob resampleJob30Km6Hr =
        new ResampleJob(
            DISTANCE_INTERPOLATION_THRESHOLD_30KM,
            TIME_INTERPOLATION_THRESHOLD_6HR,
            resampleLambdaConfiguration,
            "30km_6hr/");

    final ResampleJob resampleJob100Km18Hr =
        new ResampleJob(
            DISTANCE_INTERPOLATION_THRESHOLD_100KM,
            TIME_INTERPOLATION_THRESHOLD_18HR,
            resampleLambdaConfiguration,
            "100km_18hr/");

    return new ArrayList<>(Arrays.asList(resampleJob30Km6Hr, resampleJob100Km18Hr));
  }
}
