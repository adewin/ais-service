package uk.gov.ukho.ais.lambda.heatmap.job.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class StepFunctionInput {

  private final String jobConfigFile;
  private final String executionId;

  @JsonCreator
  public StepFunctionInput(
      @JsonProperty("jobConfigFile") final String jobConfigFile,
      @JsonProperty("executionId") final String executionId) {
    this.jobConfigFile = jobConfigFile;
    this.executionId = executionId;
  }

  public String getJobConfigFile() {
    return jobConfigFile;
  }

  public String getExecutionId() {
    return executionId;
  }
}
