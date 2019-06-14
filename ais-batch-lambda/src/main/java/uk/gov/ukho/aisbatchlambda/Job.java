package uk.gov.ukho.aisbatchlambda;

public class Job {
  private final Double resolution;
  private final String prefix;
  private final Long distanceInterpolationThreshold;
  private final Long timeInterpolationThreshold;

  public Job(
      final Double resolution,
      final String prefix,
      final Long distanceInterpolationThreshold,
      final Long timeInterpolationThreshold) {
    this.resolution = resolution;
    this.prefix = prefix;
    this.distanceInterpolationThreshold = distanceInterpolationThreshold;
    this.timeInterpolationThreshold = timeInterpolationThreshold;
  }

  public Double getResolution() {
    return resolution;
  }

  public String getPrefix() {
    return prefix;
  }

  public Long getDistanceInterpolationThreshold() {
    return distanceInterpolationThreshold;
  }

  public Long getTimeInterpolationThreshold() {
    return timeInterpolationThreshold;
  }
}
