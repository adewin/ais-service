package uk.gov.ukho.ais.validatenewjobconfiglambda.repository;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import cyclops.control.Either;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import org.apache.http.client.methods.HttpRequestBase;
import org.assertj.core.api.SoftAssertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.ukho.ais.validatenewjobconfiglambda.model.JobConfig;
import uk.gov.ukho.ais.validatenewjobconfiglambda.model.ValidationFailure;

@RunWith(MockitoJUnitRunner.class)
public class JobConfigRepositoryTest {

  @Mock private AmazonS3 mockS3Client;

  @Mock private S3Object mockS3Object;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Test
  public void whenJobConfigExistsThenReturnsSomeJobConfig() {
    SoftAssertions.assertSoftly(
        softly -> {
          final JobConfigRepository jobConfigRepository =
              new JobConfigRepository(mockS3Client, objectMapper);
          final String s3Uri = "s3://test/example.json";
          final String testConfig = "{}";
          final InputStream inputStream =
              new ByteArrayInputStream(testConfig.getBytes(Charset.defaultCharset()));

          when(mockS3Client.getObject("test", "example.json")).thenReturn(mockS3Object);
          when(mockS3Object.getObjectContent())
              .thenReturn(new S3ObjectInputStream(inputStream, mock(HttpRequestBase.class)));

          final Either<ValidationFailure, JobConfig> jobConfigOption =
              jobConfigRepository.getJobConfig(s3Uri);

          softly.assertThat(jobConfigOption.isLeft()).isFalse();
          softly.assertThat(jobConfigOption.orElse(null)).isNotNull();
        });
  }

  @Test
  public void whenJobConfigExistsThenJobConfigIsReadFromS3() {
    SoftAssertions.assertSoftly(
        softly -> {
          final JobConfigRepository jobConfigRepository =
              new JobConfigRepository(mockS3Client, objectMapper);
          final String s3Uri = "s3://test/example.json";
          final JobConfig jobConfig = new JobConfig("output", 2019, 9, "filterSqlFile");
          final String testConfig = jobConfigToJson(jobConfig);
          final InputStream inputStream =
              new ByteArrayInputStream(testConfig.getBytes(Charset.defaultCharset()));

          when(mockS3Client.getObject("test", "example.json")).thenReturn(mockS3Object);
          when(mockS3Object.getObjectContent())
              .thenReturn(new S3ObjectInputStream(inputStream, mock(HttpRequestBase.class)));

          final Either<ValidationFailure, JobConfig> jobConfigOption =
              jobConfigRepository.getJobConfig(s3Uri);

          softly.assertThat(jobConfigOption.orElse(null)).isEqualToComparingFieldByField(jobConfig);

          verify(mockS3Client, times(1)).getObject("test", "example.json");
        });
  }

  @Test
  public void whenJobConfigDoesNotExistThenJobConfigDoesNotExistValidationFailureReturned() {
    SoftAssertions.assertSoftly(
        softly -> {
          final JobConfigRepository jobConfigRepository =
              new JobConfigRepository(mockS3Client, objectMapper);
          final String s3Uri = "s3://test/example.json";

          when(mockS3Client.getObject("test", "example.json"))
              .thenThrow(new AmazonS3Exception("not found"));

          final Either<ValidationFailure, JobConfig> jobConfigOption =
              jobConfigRepository.getJobConfig(s3Uri);

          softly.assertThat(jobConfigOption.isLeft()).isTrue();
          softly
              .assertThat(jobConfigOption.leftOrElse(null))
              .isEqualTo(ValidationFailure.JOB_CONFIG_DOES_NOT_EXIST);
        });
  }

  @Test
  public void whenJobConfigIsNotValidJsonThenJobConfigNotValidJsonValidationFailureReturned() {
    SoftAssertions.assertSoftly(
        softly -> {
          final JobConfigRepository jobConfigRepository =
              new JobConfigRepository(mockS3Client, objectMapper);

          final String s3Uri = "s3://test/example.json";
          final String testConfig = "NOT JSON";
          final InputStream inputStream =
              new ByteArrayInputStream(testConfig.getBytes(Charset.defaultCharset()));

          when(mockS3Client.getObject("test", "example.json")).thenReturn(mockS3Object);
          when(mockS3Object.getObjectContent())
              .thenReturn(new S3ObjectInputStream(inputStream, mock(HttpRequestBase.class)));

          final Either<ValidationFailure, JobConfig> jobConfigOption =
              jobConfigRepository.getJobConfig(s3Uri);

          softly.assertThat(jobConfigOption.isLeft()).isTrue();
          softly
              .assertThat(jobConfigOption.leftOrElse(null))
              .isEqualTo(ValidationFailure.JOB_CONFIG_NOT_VALID_JSON);
        });
  }

  private String jobConfigToJson(final JobConfig jobConfig) {
    try {
      return objectMapper.writeValueAsString(jobConfig);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }
}
