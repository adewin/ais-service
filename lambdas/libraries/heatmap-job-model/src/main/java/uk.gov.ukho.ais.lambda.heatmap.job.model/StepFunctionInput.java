package uk.gov.ukho.ais.lambda.heatmap.job.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class StepFunctionInput {

  private final String jobConfigFile;

  @JsonCreator
  public StepFunctionInput(@JsonProperty("jobConfigFile") final String jobConfigFile) {
    this.jobConfigFile = jobConfigFile;
  }

  public String getJobConfigFile() {
    return jobConfigFile;
  }
}
