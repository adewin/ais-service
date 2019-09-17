package uk.gov.ukho.ais.lambda.handleheatmapoutcome;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import cyclops.control.Option;
import cyclops.control.Try;
import org.assertj.core.api.SoftAssertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.ukho.ais.lambda.handleheatmapoutcome.result.StepFunctionS3ObjectKeyService;
import uk.gov.ukho.ais.lambda.heatmap.job.model.JobConfig;
import uk.gov.ukho.ais.lambda.heatmap.job.model.StepFunctionOutcome;
import uk.gov.ukho.ais.lambda.heatmap.job.model.StepFunctionOutput;

@RunWith(MockitoJUnitRunner.class)
public class JobConfigSubmissionRepositoryTest {

  @Mock private AmazonS3 mockAmazonS3;
  @Mock private ObjectMapper mockObjectMapper;
  @Mock private StepFunctionS3ObjectKeyService mockS3ObjectKeyService;
  private JobConfigSubmissionRepository jobConfigSubmissionRepository;

  private final String jobSubmissionBucket = "test-bucket";
  private final StepFunctionOutput stepFunctionOutput =
      new StepFunctionOutput(
          "2345",
          StepFunctionOutcome.SUCCESS,
          "s3://bucket/test.json",
          Option.of(JobConfig.empty()),
          Option.none(),
          Option.none());
  private final String stepFunctionOutputJson = "{\"stepFunctionOutcome\": \"SUCCESS\"}";
  private final String outputKey = "completed/test.json";

  @Before
  public void setUp() throws Exception {
    jobConfigSubmissionRepository =
        new JobConfigSubmissionRepository(
            mockAmazonS3, mockObjectMapper, mockS3ObjectKeyService, jobSubmissionBucket);
  }

  @Test
  public void whenSuccessfullyWritingOutputFileToS3ThenOutputReturned()
      throws JsonProcessingException {
    setupMocksForSuccessfulRequest();

    SoftAssertions.assertSoftly(
        softly -> {
          final Try<StepFunctionOutput, Exception> result =
              jobConfigSubmissionRepository.writeOutput(stepFunctionOutput);

          softly.assertThat(result.isSuccess()).isTrue();

          softly.assertThat(result.get().orElse(null)).isEqualTo(stepFunctionOutput);
        });
  }

  @Test
  public void whenWritingOutputToS3ThenResultIsSerialisedToJson() throws Exception {
    setupMocksForSuccessfulRequest();

    jobConfigSubmissionRepository.writeOutput(stepFunctionOutput);

    verify(mockObjectMapper, times(1)).writeValueAsString(stepFunctionOutput);
  }

  @Test
  public void whenWritingOutputToS3ThenResultIsPutInS3Bucket() throws Exception {
    setupMocksForSuccessfulRequest();

    jobConfigSubmissionRepository.writeOutput(stepFunctionOutput);

    verify(mockAmazonS3, times(1))
        .putObject(jobSubmissionBucket, outputKey, stepFunctionOutputJson);
  }

  @Test
  public void whenWritingOutputToS3ButCannotWriteJsonThenExceptionReturned()
      throws JsonProcessingException {
    final RuntimeException cannotWriteJsonException = setupMockObjectMapperToThrowException();

    SoftAssertions.assertSoftly(
        softly -> {
          final Try<StepFunctionOutput, Exception> result =
              jobConfigSubmissionRepository.writeOutput(stepFunctionOutput);

          softly.assertThat(result.isFailure()).isTrue();
          softly.assertThat(result.failureGet().orElse(null)).isEqualTo(cannotWriteJsonException);
        });
  }

  private RuntimeException setupMockObjectMapperToThrowException() throws JsonProcessingException {
    final RuntimeException cannotWriteJsonException = new RuntimeException("Cannot write JSON");
    when(mockObjectMapper.writeValueAsString(stepFunctionOutput))
        .thenThrow(cannotWriteJsonException);
    return cannotWriteJsonException;
  }

  @Test
  public void whenWritingOutputToS3ButCannotWriteJsonThenNotWrittenToS3()
      throws JsonProcessingException {
    final RuntimeException cannotWriteJsonException = setupMockObjectMapperToThrowException();

    jobConfigSubmissionRepository.writeOutput(stepFunctionOutput);

    verify(mockAmazonS3, never()).putObject(anyString(), anyString(), anyString());
  }

  @Test
  public void whenThereIsAnErrorWritingOutputToS3ThenExceptionReturned()
      throws JsonProcessingException {
    final AmazonS3Exception amazonS3Failure = new AmazonS3Exception("Cannot write to S3");
    setupMocksForS3ErrorOnPutObject(amazonS3Failure);

    SoftAssertions.assertSoftly(
        softly -> {
          final Try<StepFunctionOutput, Exception> result =
              jobConfigSubmissionRepository.writeOutput(stepFunctionOutput);

          softly.assertThat(result.isFailure()).isTrue();
          softly.assertThat(result.failureGet().orElse(null)).isEqualTo(amazonS3Failure);
        });
  }

  @Test
  public void whenSuccessfullyRemovingProcessedJobConfigThenJobConfigRemovedFromS3() {
    SoftAssertions.assertSoftly(
        softly -> {
          final Try<Void, Exception> result =
              jobConfigSubmissionRepository.removeProcessedJobConfig(stepFunctionOutput);

          softly.assertThat(result.isSuccess()).isTrue();

          verify(mockAmazonS3, times(1)).deleteObject("bucket", "test.json");
        });
  }

  @Test
  public void whenThereIsAnErrorProcessingJobConfigThenIsFailure() {
    SoftAssertions.assertSoftly(
        softly -> {
          final AmazonS3Exception amazonS3Exception =
              new AmazonS3Exception("Error deleting object");
          doThrow(amazonS3Exception).when(mockAmazonS3).deleteObject("bucket", "test.json");

          final Try<Void, Exception> result =
              jobConfigSubmissionRepository.removeProcessedJobConfig(stepFunctionOutput);

          softly.assertThat(result.isFailure()).isTrue();
        });
  }

  @Test
  public void whenThereIsAnErrorProcessingJobConfigThenReturnsException() {
    SoftAssertions.assertSoftly(
        softly -> {
          final AmazonS3Exception amazonS3Exception =
              new AmazonS3Exception("Error deleting object");
          doThrow(amazonS3Exception).when(mockAmazonS3).deleteObject("bucket", "test.json");

          final Try<Void, Exception> result =
              jobConfigSubmissionRepository.removeProcessedJobConfig(stepFunctionOutput);

          softly.assertThat(result.failureGet().orElse(null)).isEqualTo(amazonS3Exception);
        });
  }

  private void setupMocksForS3ErrorOnPutObject(AmazonS3Exception amazonS3Failure)
      throws JsonProcessingException {
    setupMocksToReturnJsonAndCreateKeySuccessfully();

    when(mockAmazonS3.putObject(jobSubmissionBucket, outputKey, stepFunctionOutputJson))
        .thenThrow(amazonS3Failure);
  }

  private void setupMocksToReturnJsonAndCreateKeySuccessfully() throws JsonProcessingException {
    when(mockObjectMapper.writeValueAsString(stepFunctionOutput))
        .thenReturn(stepFunctionOutputJson);
    when(mockS3ObjectKeyService.createS3CompletedObjectKey(stepFunctionOutput))
        .thenReturn(outputKey);
  }

  private void setupMocksForSuccessfulRequest() throws JsonProcessingException {
    setupMocksToReturnJsonAndCreateKeySuccessfully();

    when(mockAmazonS3.putObject(jobSubmissionBucket, outputKey, stepFunctionOutputJson))
        .thenReturn(new PutObjectResult());
  }
}
