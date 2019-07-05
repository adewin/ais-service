package uk.gov.ukho.ais.emrjobrunner.model;

import java.util.List;

public abstract class AbstractJob {

  private final boolean active;

  protected AbstractJob(boolean active) {
    this.active = active;
  }

  public abstract List<String> getJobSpecificParameters();

  public boolean isActive() {
    return active;
  }
}
