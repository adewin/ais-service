package uk.gov.ukho.aisbatchlambda;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import uk.gov.ukho.ais.emrjobrunner.model.AbstractJob;

public class HeatmapJob extends AbstractJob {
  private final double resolution;
  private final String prefix;
  private final long distanceInterpolationThreshold;
  private final long timeInterpolationThreshold;
  private final String startPeriod;
  private final String endPeriod;
  private final String inputLocation;
  private final boolean isOutputLocationSensitive;
  private final String draughtIndex;

  public HeatmapJob(
      final String prefix,
      final double resolution,
      final long distanceInterpolationThreshold,
      final long timeInterpolationThreshold,
      final String startPeriod,
      final String endPeriod,
      final String inputLocation,
      final boolean isOutputLocationSensitive,
      final boolean active,
      final String draughtIndex) {
    super(active);
    this.resolution = resolution;
    this.prefix = prefix;
    this.distanceInterpolationThreshold = distanceInterpolationThreshold;
    this.timeInterpolationThreshold = timeInterpolationThreshold;
    this.startPeriod = startPeriod;
    this.endPeriod = endPeriod;
    this.inputLocation = inputLocation;
    this.isOutputLocationSensitive = isOutputLocationSensitive;
    this.draughtIndex = draughtIndex;
  }

  public HeatmapJob(
      final String prefix,
      final double resolution,
      final long distanceInterpolationThreshold,
      final long timeInterpolationThreshold,
      final String startPeriod,
      final String endPeriod,
      final String inputLocation,
      final boolean isOutputLocationSensitive,
      final boolean active) {
    this(
        prefix,
        resolution,
        distanceInterpolationThreshold,
        timeInterpolationThreshold,
        startPeriod,
        endPeriod,
        inputLocation,
        isOutputLocationSensitive,
        active,
        null);
  }

  public double getResolution() {
    return resolution;
  }

  public String getPrefix() {
    return prefix;
  }

  public long getDistanceInterpolationThreshold() {
    return distanceInterpolationThreshold;
  }

  public long getTimeInterpolationThreshold() {
    return timeInterpolationThreshold;
  }

  public String getStartPeriod() {
    return startPeriod;
  }

  public String getEndPeriod() {
    return endPeriod;
  }

  public String getInputLocation() {
    return inputLocation;
  }

  public boolean getIsOutputLocationSensitive() {
    return isOutputLocationSensitive;
  }

  public String getDraughtIndex() {
    return draughtIndex;
  }

  @Override
  public List<String> getJobSpecificParameters() {
    final List<String> args =
        new ArrayList<>(
            Arrays.asList(
                "--class",
                AisLambdaConfiguration.JOB_FULLY_QUALIFIED_CLASS_NAME,
                AisLambdaConfiguration.JOB_LOCATION,
                "-i",
                AisLambdaConfiguration.INPUT_LOCATION,
                "-o",
                getIsOutputLocationSensitive()
                    ? AisLambdaConfiguration.SENSITIVE_OUTPUT_LOCATION
                    : AisLambdaConfiguration.DEFAULT_OUTPUT_LOCATION,
                "-p",
                getPrefix(),
                "-r",
                String.valueOf(getResolution()),
                "-d",
                String.valueOf(getDistanceInterpolationThreshold()),
                "-t",
                String.valueOf(getTimeInterpolationThreshold()),
                "-s",
                getStartPeriod(),
                "-e",
                getEndPeriod(),
                "--draughtConfigFile",
                AisLambdaConfiguration.DRAUGHT_CONFIG_FILE,
                "--staticDataFile",
                AisLambdaConfiguration.STATIC_DATA_FILE));

    if (getDraughtIndex() != null) {
      args.addAll(Arrays.asList("--draughtIndex", getDraughtIndex()));
    }

    return args;
  }
}
