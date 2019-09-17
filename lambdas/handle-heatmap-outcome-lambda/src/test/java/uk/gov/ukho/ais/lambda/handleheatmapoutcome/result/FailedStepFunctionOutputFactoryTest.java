package uk.gov.ukho.ais.lambda.handleheatmapoutcome.result;

import cyclops.control.Either;
import cyclops.control.Option;
import java.util.Collections;
import org.assertj.core.api.SoftAssertions;
import org.junit.Test;
import uk.gov.ukho.ais.lambda.heatmap.job.model.HeatmapRequestOutcome;
import uk.gov.ukho.ais.lambda.heatmap.job.model.JobConfig;
import uk.gov.ukho.ais.lambda.heatmap.job.model.StepFunctionOutput;
import uk.gov.ukho.ais.lambda.heatmap.job.model.validation.ValidationFailure;
import uk.gov.ukho.ais.lambda.heatmap.job.model.validation.ValidationResult;

public class FailedStepFunctionOutputFactoryTest {

  private final FailedStepFunctionOutputFactory failedStepFunctionOutputFactory =
      new FailedStepFunctionOutputFactory(
          "validationFailureReason",
          "errorValidatingRequestFailureReason",
          "errorGeneratingHeatmapsFailureReason",
          "errorAggregatingMonthlyHeatmapsFailureReason");

  private final ValidationFailure jobConfigDoesNotExist =
      ValidationFailure.JOB_CONFIG_DOES_NOT_EXIST;

  private final HeatmapRequestOutcome successfulOutcome =
      new HeatmapRequestOutcome(
          "56365",
          "s3://test/submit/jobconf.json",
          new ValidationResult(Option.of(JobConfig.empty()), Collections.emptyList()),
          new Object(),
          new Object(),
          new Object(),
          null,
          null,
          null);

  private final HeatmapRequestOutcome failedValidation =
      new HeatmapRequestOutcome(
          "3563465",
          "s3://bucket/file",
          new ValidationResult(Option.none(), Collections.singletonList(jobConfigDoesNotExist)),
          null,
          null,
          null,
          null,
          null,
          null);

  private final HeatmapRequestOutcome couldNotPerformValidation =
      new HeatmapRequestOutcome(
          "", "s3://bucket/file", null, null, null, null, "Could not run validation", null, null);
  private final HeatmapRequestOutcome failedGeneratingHeatmap =
      new HeatmapRequestOutcome(
          "",
          "s3://bucket/file",
          new ValidationResult(Option.of(JobConfig.empty()), Collections.emptyList()),
          null,
          null,
          null,
          null,
          "Failed generating heatmaps",
          null);

  private final HeatmapRequestOutcome failedAggregatingHeatmaps =
      new HeatmapRequestOutcome(
          "",
          "s3://bucket/file",
          new ValidationResult(Option.of(JobConfig.empty()), Collections.emptyList()),
          null,
          null,
          null,
          null,
          null,
          "failed aggregating heatmaps");

  @Test
  public void whenJobConfigHasBeenValidatedAndHeatmapsGeneratedThenIsNotFailed() {
    SoftAssertions.assertSoftly(
        softly -> {
          final boolean result = failedStepFunctionOutputFactory.hasFailed(successfulOutcome);

          softly.assertThat(result).isFalse();
        });
  }

  @Test
  public void whenOutcomeHasFailedValidationThenIsFailed() {
    SoftAssertions.assertSoftly(
        softly -> {
          final boolean result = failedStepFunctionOutputFactory.hasFailed(failedValidation);

          softly.assertThat(result).isTrue();
        });
  }

  @Test
  public void whenOutcomeCouldNotRunValidationThenIsFailed() {
    SoftAssertions.assertSoftly(
        softly -> {
          final boolean result =
              failedStepFunctionOutputFactory.hasFailed(couldNotPerformValidation);

          softly.assertThat(result).isTrue();
        });
  }

  @Test
  public void whenErrorGeneratingMonthlyHeatmapsThenIsFailed() {
    SoftAssertions.assertSoftly(
        softly -> {
          final boolean result = failedStepFunctionOutputFactory.hasFailed(failedGeneratingHeatmap);

          softly.assertThat(result).isTrue();
        });
  }

  @Test
  public void whenErrorPerformingAggregationThenIsFailed() {
    SoftAssertions.assertSoftly(
        softly -> {
          final boolean result =
              failedStepFunctionOutputFactory.hasFailed(failedAggregatingHeatmaps);

          softly.assertThat(result).isTrue();
        });
  }

  @Test
  public void whenCreatingFailedResultForValidationFailureThenHasFailureReasonOfValidationFailed() {
    SoftAssertions.assertSoftly(
        softly -> {
          final StepFunctionOutput result =
              failedStepFunctionOutputFactory.createStepFunctionOutput(failedValidation);

          softly.assertThat(result.getFailureReason().isPresent()).isTrue();
          softly
              .assertThat(result.getFailureReason().orElse(null))
              .isEqualTo("validationFailureReason");
        });
  }

  @Test
  public void whenCreatingFailedResultForValidationFailureThenResultIncludesValidationFailures() {
    SoftAssertions.assertSoftly(
        softly -> {
          final StepFunctionOutput result =
              failedStepFunctionOutputFactory.createStepFunctionOutput(failedValidation);

          softly.assertThat(result.getError().isPresent()).isTrue();
          softly
              .assertThat(
                  result.getError().orElse(Either.right("")).leftOrElse(Collections.emptyList()))
              .containsExactly(jobConfigDoesNotExist);
        });
  }

  @Test
  public void
      whenCreatingFailedResultForWhenValidationCouldNotBeRunThenHasFailureReasonOfErrorPerformingValidation() {
    SoftAssertions.assertSoftly(
        softly -> {
          final StepFunctionOutput result =
              failedStepFunctionOutputFactory.createStepFunctionOutput(couldNotPerformValidation);

          softly.assertThat(result.getFailureReason().isPresent()).isTrue();
          softly
              .assertThat(result.getFailureReason().orElse(null))
              .isEqualTo("errorValidatingRequestFailureReason");
        });
  }

  @Test
  public void
      whenCreatingFailedResultForWhenValidationCouldNotBeRunThenResultIncludesErrorEncounteredValidatingRequest() {
    SoftAssertions.assertSoftly(
        softly -> {
          final StepFunctionOutput result =
              failedStepFunctionOutputFactory.createStepFunctionOutput(couldNotPerformValidation);

          softly.assertThat(result.getFailureReason().isPresent()).isTrue();
          softly
              .assertThat(
                  result.getError().orElse(Either.left(Collections.emptyList())).orElse(null))
              .isEqualTo("Could not run validation");
        });
  }

  @Test
  public void
      whenCreatingFailedResultForHeatmapGenerationErrorThenHasFailureReasonOfErrorGeneratingMonthlyHeatmaps() {
    SoftAssertions.assertSoftly(
        softly -> {
          final StepFunctionOutput result =
              failedStepFunctionOutputFactory.createStepFunctionOutput(failedGeneratingHeatmap);

          softly.assertThat(result.getFailureReason().isPresent()).isTrue();
          softly
              .assertThat(result.getFailureReason().orElse(null))
              .isEqualTo("errorGeneratingHeatmapsFailureReason");
        });
  }

  @Test
  public void
      whenCreatingFailedResultForHeatmapGenerationErrorThenResultIncludesErrorGeneratingMonthlyHeatmaps() {
    SoftAssertions.assertSoftly(
        softly -> {
          final StepFunctionOutput result =
              failedStepFunctionOutputFactory.createStepFunctionOutput(failedGeneratingHeatmap);

          softly.assertThat(result.getFailureReason().isPresent()).isTrue();
          softly
              .assertThat(
                  result.getError().orElse(Either.left(Collections.emptyList())).orElse(null))
              .isEqualTo("Failed generating heatmaps");
        });
  }

  @Test
  public void
      whenCreatingFailedResultForHeatmapAggregationErrorThenHasFailureReasonOfErrorAggregatingMonthlyHeatmaps() {
    SoftAssertions.assertSoftly(
        softly -> {
          final StepFunctionOutput result =
              failedStepFunctionOutputFactory.createStepFunctionOutput(failedAggregatingHeatmaps);

          softly.assertThat(result.getFailureReason().isPresent()).isTrue();
          softly
              .assertThat(result.getFailureReason().orElse(null))
              .isEqualTo("errorAggregatingMonthlyHeatmapsFailureReason");
        });
  }

  @Test
  public void
      whenCreatingFailedResultForHeatmapAggregationErrorThenResultIncludesErrorAggregatingMonthlyHeatmaps() {
    SoftAssertions.assertSoftly(
        softly -> {
          final StepFunctionOutput result =
              failedStepFunctionOutputFactory.createStepFunctionOutput(failedAggregatingHeatmaps);

          softly.assertThat(result.getFailureReason().isPresent()).isTrue();
          softly
              .assertThat(
                  result.getError().orElse(Either.left(Collections.emptyList())).orElse(null))
              .isEqualTo("failed aggregating heatmaps");
        });
  }
}
