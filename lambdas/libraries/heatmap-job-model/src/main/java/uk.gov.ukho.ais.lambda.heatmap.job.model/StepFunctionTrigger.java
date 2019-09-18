package uk.gov.ukho.ais.lambda.heatmap.job.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class StepFunctionTrigger {

  private final String jobConfigFile;

  public StepFunctionTrigger(@JsonProperty("jobConfigFile") final String jobConfigFile) {
    this.jobConfigFile = jobConfigFile;
  }

  public String getJobConfigFile() {
    return jobConfigFile;
  }
}
