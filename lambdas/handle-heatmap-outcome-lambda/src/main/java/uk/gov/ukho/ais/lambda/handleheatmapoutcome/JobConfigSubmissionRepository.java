package uk.gov.ukho.ais.lambda.handleheatmapoutcome;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3URI;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import cyclops.control.Try;
import java.util.function.Function;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.ukho.ais.lambda.handleheatmapoutcome.result.StepFunctionS3ObjectKeyService;
import uk.gov.ukho.ais.lambda.heatmap.job.model.StepFunctionOutput;

@Component
public class JobConfigSubmissionRepository {

  private final AmazonS3 amazonS3;
  private final ObjectMapper objectMapper;
  private final StepFunctionS3ObjectKeyService stepFunctionS3ObjectKeyService;
  private final String s3JobSubmissionBucket;

  public JobConfigSubmissionRepository(
      final AmazonS3 amazonS3,
      final ObjectMapper objectMapper,
      final StepFunctionS3ObjectKeyService stepFunctionS3ObjectKeyService,
      @Value("${JOB_SUBMISSION_BUCKET_NAME}") final String s3JobSubmissionBucket) {
    this.amazonS3 = amazonS3;
    this.objectMapper = objectMapper;
    this.stepFunctionS3ObjectKeyService = stepFunctionS3ObjectKeyService;
    this.s3JobSubmissionBucket = s3JobSubmissionBucket;
  }

  public Try<StepFunctionOutput, Exception> writeOutput(
      final StepFunctionOutput stepFunctionOutput) {
    return Try.withCatch(() -> objectMapper.writeValueAsString(stepFunctionOutput), Exception.class)
        .flatMap(writeToS3(stepFunctionOutput))
        .map(putObjectResult -> stepFunctionOutput);
  }

  public Try<Void, Exception> removeProcessedJobConfig(
      final StepFunctionOutput stepFunctionOutput) {
    final AmazonS3URI amazonS3URI = new AmazonS3URI(stepFunctionOutput.getInputS3Uri());
    return Try.runWithCatch(
        () -> amazonS3.deleteObject(amazonS3URI.getBucket(), amazonS3URI.getKey()));
  }

  private Function<String, Try<PutObjectResult, Exception>> writeToS3(
      final StepFunctionOutput stepFunctionOutput) {
    return json ->
        Try.withCatch(
            () ->
                amazonS3.putObject(
                    s3JobSubmissionBucket,
                    stepFunctionS3ObjectKeyService.createS3CompletedObjectKey(stepFunctionOutput),
                    json),
            AmazonS3Exception.class);
  }
}
