package uk.gov.ukho.ais.lambda.handleheatmapoutcome.result;

import cyclops.control.Either;
import cyclops.control.Option;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.ukho.ais.lambda.heatmap.job.model.HeatmapRequestOutcome;
import uk.gov.ukho.ais.lambda.heatmap.job.model.StepFunctionOutcome;
import uk.gov.ukho.ais.lambda.heatmap.job.model.StepFunctionOutput;
import uk.gov.ukho.ais.lambda.heatmap.job.model.validation.ValidationFailure;
import uk.gov.ukho.ais.lambda.heatmap.job.model.validation.ValidationResult;

@Component
public class FailedStepFunctionOutputFactory {

  private final String validationFailureReason;
  private final String errorValidatingRequestFailureReason;
  private final String errorGeneratingHeatmapsFailureReason;
  private final String errorAggregatingMonthlyHeatmapsFailureReason;

  public FailedStepFunctionOutputFactory(
      @Value("${failure.reason.validationFailed}") final String validationFailureReason,
      @Value("${failure.reason.validationError}") final String errorValidatingRequestFailureReason,
      @Value("${failure.reason.generationError}") final String errorGeneratingHeatmapsFailureReason,
      @Value("${failure.reason.aggregationError}")
          final String errorAggregatingMonthlyHeatmapsFailureReason) {
    this.validationFailureReason = validationFailureReason;
    this.errorValidatingRequestFailureReason = errorValidatingRequestFailureReason;
    this.errorGeneratingHeatmapsFailureReason = errorGeneratingHeatmapsFailureReason;
    this.errorAggregatingMonthlyHeatmapsFailureReason =
        errorAggregatingMonthlyHeatmapsFailureReason;
  }

  public StepFunctionOutput createStepFunctionOutput(
      final HeatmapRequestOutcome heatmapRequestOutcome) {
    final String failureReason = determineFailureReason(heatmapRequestOutcome);

    final Either<List<ValidationFailure>, Object> error =
        heatmapRequestOutcome
            .getValidationResult()
            .filter(validationResult -> !validationResult.isSuccess())
            .map(ValidationResult::getError)
            .map(Either::left)
            .orElse(Either.right(determineErrorForNonValidationFailure(heatmapRequestOutcome)));

    return new StepFunctionOutput(
        heatmapRequestOutcome.getExecutionId(),
        StepFunctionOutcome.FAILED,
        heatmapRequestOutcome.getJobConfigFile(),
        JobConfigOutputCreator.createOutputJobConfig(heatmapRequestOutcome),
        Option.of(failureReason),
        Option.of(error));
  }

  public boolean hasFailed(final HeatmapRequestOutcome heatmapRequestOutcome) {
    return !hasPassedValidation(heatmapRequestOutcome)
        || heatmapRequestOutcome.getValidationFailure().isPresent()
        || heatmapRequestOutcome.getHeatmapGenerationFailure().isPresent()
        || heatmapRequestOutcome.getHeatmapAggregationFailure().isPresent();
  }

  private String determineFailureReason(final HeatmapRequestOutcome heatmapRequestOutcome) {
    return heatmapRequestOutcome
        .getValidationResult()
        .filter(validationResult -> !validationResult.isSuccess())
        .map(validationResult -> validationFailureReason)
        .orElseUse(
            heatmapRequestOutcome
                .getValidationFailure()
                .map(failure -> errorValidatingRequestFailureReason))
        .orElseUse(
            heatmapRequestOutcome
                .getHeatmapGenerationFailure()
                .map(failure -> errorGeneratingHeatmapsFailureReason))
        .orElseUse(
            heatmapRequestOutcome
                .getHeatmapAggregationFailure()
                .map(failure -> errorAggregatingMonthlyHeatmapsFailureReason))
        .orElse("Unknown error");
  }

  private Object determineErrorForNonValidationFailure(
      final HeatmapRequestOutcome heatmapRequestOutcome) {
    return heatmapRequestOutcome
        .getValidationFailure()
        .orElseUse(heatmapRequestOutcome.getHeatmapGenerationFailure())
        .orElseUse(heatmapRequestOutcome.getHeatmapAggregationFailure())
        .orElse("Unknown error");
  }

  private boolean hasPassedValidation(final HeatmapRequestOutcome heatmapRequestOutcome) {
    return heatmapRequestOutcome
        .getValidationResult()
        .map(ValidationResult::isSuccess)
        .orElse(false);
  }
}
