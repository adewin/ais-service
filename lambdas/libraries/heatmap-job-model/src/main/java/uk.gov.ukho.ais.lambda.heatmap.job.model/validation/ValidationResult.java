package uk.gov.ukho.ais.lambda.heatmap.job.model.validation;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import uk.gov.ukho.ais.lambda.heatmap.job.model.JobConfig;

public class ValidationResult {

  private final JobConfig jobConfig;
  private final List<ValidationFailure> error;
  private final boolean success;

  @JsonCreator
  public ValidationResult(
      @JsonProperty("jobConfig") final JobConfig jobConfig,
      @JsonProperty("error") final List<ValidationFailure> error) {
    this.jobConfig = jobConfig;
    this.error = error;
    this.success = this.error.isEmpty();
  }

  public JobConfig getJobConfig() {
    return jobConfig;
  }

  public List<ValidationFailure> getError() {
    return error;
  }

  public boolean isSuccess() {
    return success;
  }
}
