package uk.gov.ukho.aisbatchlambda;

public class Job {
  private final double resolution;
  private final String prefix;
  private final long distanceInterpolationThreshold;
  private final long timeInterpolationThreshold;
  private final String startPeriod;
  private final String endPeriod;
  private final String inputLocation;
  private final boolean isOutputLocationSensitive;
  private final String draughtIndex;
  private final boolean active;

  public Job(
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
    this.resolution = resolution;
    this.prefix = prefix;
    this.distanceInterpolationThreshold = distanceInterpolationThreshold;
    this.timeInterpolationThreshold = timeInterpolationThreshold;
    this.startPeriod = startPeriod;
    this.endPeriod = endPeriod;
    this.inputLocation = inputLocation;
    this.isOutputLocationSensitive = isOutputLocationSensitive;
    this.draughtIndex = draughtIndex;
    this.active = active;
  }

  public Job(
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

  public boolean isActive() {
    return active;
  }
}
