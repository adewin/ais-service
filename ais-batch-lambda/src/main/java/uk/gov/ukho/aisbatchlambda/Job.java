package uk.gov.ukho.aisbatchlambda;

public class Job {
  private final Double resolution;
  private final String prefix;
  private final Long distanceInterpolationThreshold;
  private final Long timeInterpolationThreshold;
  private final String startPeriod;
  private final String endPeriod;

  public Job(
      final Double resolution,
      final String prefix,
      final Long distanceInterpolationThreshold,
      final Long timeInterpolationThreshold,
      final String startPeriod,
      final String endPeriod) {
    this.resolution = resolution;
    this.prefix = prefix;
    this.distanceInterpolationThreshold = distanceInterpolationThreshold;
    this.timeInterpolationThreshold = timeInterpolationThreshold;
    this.startPeriod = startPeriod;
    this.endPeriod = endPeriod;
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

  public String getStartPeriod() {
    return startPeriod;
  }

  public String getEndPeriod() {
    return endPeriod;
  }
}
