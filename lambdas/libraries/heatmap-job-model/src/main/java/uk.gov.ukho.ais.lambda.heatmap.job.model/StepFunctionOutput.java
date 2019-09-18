package uk.gov.ukho.ais.lambda.heatmap.job.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import cyclops.control.Either;
import cyclops.control.Option;
import java.util.List;
import uk.gov.ukho.ais.lambda.heatmap.job.model.validation.ValidationFailure;

public class StepFunctionOutput {

  private final JobConfig jobConfig;
  private final String inputS3Uri;
  private final String executionId;
  private final StepFunctionOutcome stepFunctionOutcome;
  private final Option<String> failureReason;
  private final Option<Either<List<ValidationFailure>, Object>> error;

  @JsonCreator
  public StepFunctionOutput(
      @JsonProperty("executionId") final String executionId,
      @JsonProperty("stepFunctionOutcome") final StepFunctionOutcome stepFunctionOutcome,
      @JsonProperty("inputS3Uri") final String inputS3Uri,
      @JsonProperty("jobConfig") final JobConfig jobConfig,
      @JsonProperty("failureReason") final Option<String> failureReason,
      @JsonProperty("error") final Option<Either<List<ValidationFailure>, Object>> error) {
    this.jobConfig = jobConfig;
    this.inputS3Uri = inputS3Uri;
    this.stepFunctionOutcome = stepFunctionOutcome;
    this.executionId = executionId;
    this.failureReason = failureReason;
    this.error = error;
  }

  public JobConfig getJobConfig() {
    return jobConfig;
  }

  public String getInputS3Uri() {
    return inputS3Uri;
  }

  public StepFunctionOutcome getStepFunctionOutcome() {
    return stepFunctionOutcome;
  }

  public Option<String> getFailureReason() {
    return failureReason;
  }

  public Option<Either<List<ValidationFailure>, Object>> getError() {
    return error;
  }

  public String getExecutionId() {
    return executionId;
  }
}
