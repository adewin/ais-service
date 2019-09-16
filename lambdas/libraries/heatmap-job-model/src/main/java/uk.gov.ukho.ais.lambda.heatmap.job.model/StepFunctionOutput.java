package uk.gov.ukho.ais.lambda.heatmap.job.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import cyclops.control.Either;
import cyclops.control.Option;
import java.util.List;
import uk.gov.ukho.ais.lambda.heatmap.job.model.validation.ValidationFailure;

public class StepFunctionOutput {

  private final Option<JobConfig> processedJobConfig;
  private final String inputS3Uri;
  private final StepFunctionOutcome stepFunctionOutcome;
  private final Option<String> failureReason;
  private final Option<Either<List<ValidationFailure>, Object>> error;

  @JsonCreator
  public StepFunctionOutput(
      @JsonProperty("stepFunctionOutcome") StepFunctionOutcome stepFunctionOutcome,
      @JsonProperty("inputS3Uri") String inputS3Uri,
      @JsonProperty("processedJobConfig") Option<JobConfig> processedJobConfig,
      @JsonProperty("failureReason") Option<String> failureReason,
      @JsonProperty("error") Option<Either<List<ValidationFailure>, Object>> error) {
    this.processedJobConfig = processedJobConfig;
    this.inputS3Uri = inputS3Uri;
    this.stepFunctionOutcome = stepFunctionOutcome;
    this.failureReason = failureReason;
    this.error = error;
  }

  public Option<JobConfig> getProcessedJobConfig() {
    return processedJobConfig;
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
}
