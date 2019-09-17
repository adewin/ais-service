package uk.gov.ukho.ais.lambda.handleheatmapoutcome.result;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

import cyclops.control.Option;
import java.util.Collections;
import org.assertj.core.api.SoftAssertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.ukho.ais.lambda.heatmap.job.model.HeatmapRequestOutcome;
import uk.gov.ukho.ais.lambda.heatmap.job.model.JobConfig;
import uk.gov.ukho.ais.lambda.heatmap.job.model.StepFunctionOutcome;
import uk.gov.ukho.ais.lambda.heatmap.job.model.StepFunctionOutput;
import uk.gov.ukho.ais.lambda.heatmap.job.model.validation.ValidationFailure;
import uk.gov.ukho.ais.lambda.heatmap.job.model.validation.ValidationResult;

@RunWith(MockitoJUnitRunner.class)
public class StepFunctionOutputFactoryTest {
  private final HeatmapRequestOutcome successfulRequest =
      new HeatmapRequestOutcome(
          "",
          "s3://test/submit/jobconf.json",
          new ValidationResult(Option.of(JobConfig.empty()), Collections.emptyList()),
          new Object(),
          new Object(),
          new Object(),
          null,
          null,
          null);
  private final HeatmapRequestOutcome failedValidationRequest =
      new HeatmapRequestOutcome(
          "",
          "s3://test/submit/jobconf.json",
          new ValidationResult(
              Option.none(),
              Collections.singletonList(ValidationFailure.JOB_CONFIG_DOES_NOT_EXIST)),
          new Object(),
          new Object(),
          new Object(),
          null,
          null,
          null);

  @Mock private FailedStepFunctionOutputFactory failedStepFunctionOutputFactory;

  @InjectMocks private StepFunctionOutputFactory stepFunctionOutputFactory;

  @Test
  public void whenProcessingSuccessfulThenReturnsSuccessResult() {
    SoftAssertions.assertSoftly(
        softly -> {
          when(failedStepFunctionOutputFactory.hasFailed(successfulRequest)).thenReturn(false);

          final StepFunctionOutput stepFunctionOutput =
              stepFunctionOutputFactory.createStepFunctionOutput(successfulRequest);

          softly
              .assertThat(stepFunctionOutput.getStepFunctionOutcome())
              .isEqualTo(StepFunctionOutcome.SUCCESS);
        });
  }

  @Test
  public void whenProcessingSuccessfulThenResultDoesNotHaveError() {
    SoftAssertions.assertSoftly(
        softly -> {
          when(failedStepFunctionOutputFactory.hasFailed(successfulRequest)).thenReturn(false);

          final StepFunctionOutput stepFunctionOutput =
              stepFunctionOutputFactory.createStepFunctionOutput(successfulRequest);

          softly.assertThat(stepFunctionOutput.getError().isPresent()).isFalse();
        });
  }

  @Test
  public void whenProcessingSuccessfulExecutionThenResultHasJobConfig() {
    SoftAssertions.assertSoftly(
        softly -> {
          when(failedStepFunctionOutputFactory.hasFailed(successfulRequest)).thenReturn(false);

          final StepFunctionOutput stepFunctionOutput =
              stepFunctionOutputFactory.createStepFunctionOutput(successfulRequest);

          softly.assertThat(stepFunctionOutput.getProcessedJobConfig().isPresent()).isTrue();
          softly
              .assertThat(stepFunctionOutput.getProcessedJobConfig().orElse(null))
              .isEqualToComparingFieldByField(JobConfig.empty());
        });
  }

  @Test
  public void whenProcessingSuccessfulExecutionThenResultHasNoFailureReason() {
    SoftAssertions.assertSoftly(
        softly -> {
          when(failedStepFunctionOutputFactory.hasFailed(successfulRequest)).thenReturn(false);

          final StepFunctionOutput stepFunctionOutput =
              stepFunctionOutputFactory.createStepFunctionOutput(successfulRequest);

          softly.assertThat(stepFunctionOutput.getFailureReason().isPresent()).isFalse();
        });
  }

  @Test
  public void
      whenProcessingSuccessfulExecutionThenFailedStepOutputFactoryNotCalledToCreateOutput() {
    when(failedStepFunctionOutputFactory.hasFailed(successfulRequest)).thenReturn(false);

    stepFunctionOutputFactory.createStepFunctionOutput(successfulRequest);

    verify(failedStepFunctionOutputFactory, never()).createStepFunctionOutput(successfulRequest);
  }

  @Test
  public void whenRequestFailedThenReturnsCreatedByFailedStepOutputFactory() {
    SoftAssertions.assertSoftly(
        softly -> {
          final StepFunctionOutput failedOutput =
              new StepFunctionOutput(
                  "abc",
                  StepFunctionOutcome.FAILED,
                  "s3://buck/1.json",
                  Option.none(),
                  Option.of("Validation Failed"),
                  Option.none());
          when(failedStepFunctionOutputFactory.hasFailed(failedValidationRequest)).thenReturn(true);
          when(failedStepFunctionOutputFactory.createStepFunctionOutput(failedValidationRequest))
              .thenReturn(failedOutput);

          final StepFunctionOutput stepFunctionOutput =
              stepFunctionOutputFactory.createStepFunctionOutput(failedValidationRequest);

          softly.assertThat(stepFunctionOutput).isEqualTo(failedOutput);

          verify(failedStepFunctionOutputFactory, times(1))
              .createStepFunctionOutput(failedValidationRequest);
        });
  }
}
