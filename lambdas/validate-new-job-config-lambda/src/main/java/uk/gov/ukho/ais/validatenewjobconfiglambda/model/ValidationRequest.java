package uk.gov.ukho.ais.validatenewjobconfiglambda.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ValidationRequest {

  private final String jobConfigFile;

  @JsonCreator
  public ValidationRequest(@JsonProperty("jobConfigFile") final String jobConfigFile) {
    this.jobConfigFile = jobConfigFile;
  }

  public String getJobConfigFile() {
    return jobConfigFile;
  }
}
