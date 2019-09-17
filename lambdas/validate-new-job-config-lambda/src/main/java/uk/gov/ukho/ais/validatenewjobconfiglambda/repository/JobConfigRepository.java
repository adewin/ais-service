package uk.gov.ukho.ais.validatenewjobconfiglambda.repository;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3URI;
import com.amazonaws.services.s3.model.S3Object;
import com.fasterxml.jackson.databind.ObjectMapper;
import cyclops.control.Either;
import java.io.IOException;
import org.springframework.stereotype.Component;
import uk.gov.ukho.ais.lambda.heatmap.job.model.JobConfig;
import uk.gov.ukho.ais.lambda.heatmap.job.model.validation.ValidationFailure;

@Component
public class JobConfigRepository {

  private final AmazonS3 s3Client;
  private final ObjectMapper objectMapper;

  public JobConfigRepository(final AmazonS3 s3Client, ObjectMapper objectMapper) {
    this.s3Client = s3Client;
    this.objectMapper = objectMapper;
  }

  public Either<ValidationFailure, JobConfig> getJobConfig(final String s3Uri) {

    final AmazonS3URI amazonS3URI = new AmazonS3URI(s3Uri);
    try {
      final S3Object s3Object = s3Client.getObject(amazonS3URI.getBucket(), amazonS3URI.getKey());
      return Either.right(objectMapper.readValue(s3Object.getObjectContent(), JobConfig.class));
    } catch (final IOException e) {
      return Either.left(ValidationFailure.JOB_CONFIG_NOT_VALID_JSON);
    } catch (final AmazonClientException e) {
      return Either.left(ValidationFailure.JOB_CONFIG_DOES_NOT_EXIST);
    }
  }
}
