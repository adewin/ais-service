package uk.gov.ukho.ais.lambda.handleheatmapoutcome.result;

import cyclops.control.Option;
import org.springframework.stereotype.Component;
import uk.gov.ukho.ais.lambda.heatmap.job.model.HeatmapRequestOutcome;
import uk.gov.ukho.ais.lambda.heatmap.job.model.StepFunctionOutcome;
import uk.gov.ukho.ais.lambda.heatmap.job.model.StepFunctionOutput;

@Component
public class StepFunctionOutputFactory {

  private final FailedStepFunctionOutputFactory failedStepFunctionOutputFactory;

  public StepFunctionOutputFactory(
      final FailedStepFunctionOutputFactory failedStepFunctionOutputFactory) {
    this.failedStepFunctionOutputFactory = failedStepFunctionOutputFactory;
  }

  public StepFunctionOutput createStepFunctionOutput(
      final HeatmapRequestOutcome heatmapRequestOutcome) {
    return Option.of(heatmapRequestOutcome)
        .filter(this::isSuccess)
        .toEither(heatmapRequestOutcome)
        .fold(this::createFailedResult, this::createSucceededResult);
  }

  private StepFunctionOutput createFailedResult(final HeatmapRequestOutcome failed) {
    return failedStepFunctionOutputFactory.createStepFunctionOutput(failed);
  }

  private StepFunctionOutput createSucceededResult(final HeatmapRequestOutcome succeeded) {
    return new StepFunctionOutput(
        succeeded.getExecutionId(),
        StepFunctionOutcome.SUCCESS,
        succeeded.getJobConfigFile(),
        JobConfigOutputCreator.createOutputJobConfig(succeeded),
        Option.none(),
        Option.none());
  }

  private boolean isSuccess(final HeatmapRequestOutcome heatmapRequestOutcome) {
    return heatmapRequestOutcome.getValidationResult().isPresent()
        && !failedStepFunctionOutputFactory.hasFailed(heatmapRequestOutcome);
  }
}
