package uk.gov.ukho.ais.triggerresamplelambda.model;

import java.util.Arrays;
import java.util.List;
import uk.gov.ukho.ais.emrjobrunner.model.AbstractJob;
import uk.gov.ukho.ais.triggerresamplelambda.configuration.ResampleLambdaConfiguration;

public class ResampleJob extends AbstractJob {

  private final long distanceThreshold;
  private final long timeThreshold;
  private final ResampleLambdaConfiguration configuration;
  private final String objectKeyPrefix;

  public ResampleJob(
      final long distanceThreshold,
      final long timeThreshold,
      final ResampleLambdaConfiguration configuration,
      final String objectKeyPrefix) {
    this.distanceThreshold = distanceThreshold;
    this.timeThreshold = timeThreshold;
    this.configuration = configuration;
    this.objectKeyPrefix = objectKeyPrefix;
  }

  @Override
  public List<String> getJobSpecificParameters() {
    return Arrays.asList(
        "--class",
        configuration.getJobFullyQualifiedClassName(),
        configuration.getJobLocation(),
        "-i",
        configuration.getInputLocation(),
        "-o",
        configuration.getOutputLocation() + "/" + objectKeyPrefix,
        "-d",
        String.valueOf(distanceThreshold),
        "-t",
        String.valueOf(timeThreshold));
  }
}
