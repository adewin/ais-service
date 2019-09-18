package uk.gov.ukho.ais.lambda.handleheatmapoutcome;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

import com.amazonaws.services.s3.model.AmazonS3Exception;
import cyclops.control.Option;
import cyclops.control.Try;
import java.util.Collections;
import org.assertj.core.api.SoftAssertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.ukho.ais.lambda.handleheatmapoutcome.result.StepFunctionOutputFactory;
import uk.gov.ukho.ais.lambda.heatmap.job.model.HeatmapRequestOutcome;
import uk.gov.ukho.ais.lambda.heatmap.job.model.JobConfig;
import uk.gov.ukho.ais.lambda.heatmap.job.model.StepFunctionOutcome;
import uk.gov.ukho.ais.lambda.heatmap.job.model.StepFunctionOutput;
import uk.gov.ukho.ais.lambda.heatmap.job.model.validation.ValidationResult;

@RunWith(MockitoJUnitRunner.class)
public class OutcomeHandlingFunctionTest {

  @Mock private StepFunctionOutputFactory mockStepFunctionOutputFactory;

  @Mock private JobConfigSubmissionRepository mockJobConfigSubmissionRepository;

  @InjectMocks private OutcomeHandlingFunction outcomeHandlingFunction;

  private final HeatmapRequestOutcome heatmapRequestOutcome =
      new HeatmapRequestOutcome(
          "55336",
          "s3://bucket/file",
          new ValidationResult(Option.of(JobConfig.empty()), Collections.emptyList()),
          true,
          new Object(),
          new Object(),
          new Object(),
          null);

  private final StepFunctionOutput stepFunctionOutput =
      new StepFunctionOutput(
          "1234",
          StepFunctionOutcome.SUCCESS,
          "s3://bucket/file",
          Option.of(JobConfig.empty()),
          Option.none(),
          Option.none());

  @Test
  public void whenHandlingOutcomeThenCreatesOutput() {
    SoftAssertions.assertSoftly(
        softly -> {
          setupMocksForSuccessfulRequest();
          final StepFunctionOutput result = outcomeHandlingFunction.apply(heatmapRequestOutcome);

          softly.assertThat(result).isEqualTo(stepFunctionOutput);

          verify(mockStepFunctionOutputFactory, times(1))
              .createStepFunctionOutput(heatmapRequestOutcome);
        });
  }

  @Test
  public void whenHandlingOutcomeThenOutputWrittenToRepository() {
    setupMocksForSuccessfulRequest();

    outcomeHandlingFunction.apply(heatmapRequestOutcome);

    verify(mockJobConfigSubmissionRepository, times(1)).writeOutput(stepFunctionOutput);
  }

  @Test
  public void whenHandlingOutcomeThenRemovesProcessedJobConfig() {
    setupMocksForSuccessfulRequest();

    outcomeHandlingFunction.apply(heatmapRequestOutcome);

    verify(mockJobConfigSubmissionRepository, times(1))
        .removeProcessedJobConfig(stepFunctionOutput);
  }

  @Test
  public void whenFailedToWriteOutcomeThenReturnsOutput() {
    SoftAssertions.assertSoftly(
        softly -> {
          setupMocksForFailureToWriteOutput();

          final StepFunctionOutput result = outcomeHandlingFunction.apply(heatmapRequestOutcome);

          softly.assertThat(result).isEqualTo(stepFunctionOutput);

          verify(mockStepFunctionOutputFactory, times(1))
              .createStepFunctionOutput(heatmapRequestOutcome);
        });
  }

  @Test
  public void whenFailedToWriteOutcomeThenDoesNotRemoveProcessedJobConfig() {
    setupMocksForFailureToWriteOutput();

    outcomeHandlingFunction.apply(heatmapRequestOutcome);

    verify(mockJobConfigSubmissionRepository, never()).removeProcessedJobConfig(stepFunctionOutput);
  }

  private void setupMocksForFailureToWriteOutput() {
    when(mockStepFunctionOutputFactory.createStepFunctionOutput(heatmapRequestOutcome))
        .thenReturn(stepFunctionOutput);
    when(mockJobConfigSubmissionRepository.writeOutput(stepFunctionOutput))
        .thenReturn(Try.failure(new AmazonS3Exception("Cannot write file")));
  }

  private void setupMocksForSuccessfulRequest() {
    when(mockStepFunctionOutputFactory.createStepFunctionOutput(heatmapRequestOutcome))
        .thenReturn(stepFunctionOutput);

    when(mockJobConfigSubmissionRepository.writeOutput(stepFunctionOutput))
        .thenReturn(Try.success(stepFunctionOutput));
    when(mockJobConfigSubmissionRepository.removeProcessedJobConfig(stepFunctionOutput))
        .thenReturn(Try.success(null));
  }
}
