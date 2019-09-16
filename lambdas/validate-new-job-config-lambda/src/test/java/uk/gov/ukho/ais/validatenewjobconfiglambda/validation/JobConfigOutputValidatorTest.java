package uk.gov.ukho.ais.validatenewjobconfiglambda.validation;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import cyclops.control.Either;
import cyclops.control.Validated;
import cyclops.data.NonEmptyList;
import org.assertj.core.api.SoftAssertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.ukho.ais.lambda.heatmap.job.model.JobConfig;
import uk.gov.ukho.ais.lambda.heatmap.job.model.validation.ValidationFailure;

@RunWith(MockitoJUnitRunner.class)
public class JobConfigOutputValidatorTest {

  @Mock private AmazonS3 mockAmazonS3;

  @InjectMocks private JobConfigOutputValidator jobConfigOutputValidator;

  @Test
  public void whenOutputSuppliedAndExistsThenReturnsValidatedResult() {
    SoftAssertions.assertSoftly(
        softly -> {
          final String outputBucketName = "output";
          final JobConfig jobConfig = new JobConfig(outputBucketName, 2019, 9, "filterSqlFile.sql");
          final Either<ValidationFailure, JobConfig> jobConfigOrFailure = Either.right(jobConfig);

          when(mockAmazonS3.doesBucketExistV2(outputBucketName)).thenReturn(true);

          final Validated<ValidationFailure, JobConfig> validated =
              jobConfigOutputValidator.validate(jobConfigOrFailure);

          softly.assertThat(validated.isValid()).isTrue();
          softly.assertThat(validated.orElse(null)).isEqualToComparingFieldByField(jobConfig);
        });
  }

  @Test
  public void whenOutputSuppliedThenS3QueriedToCheckOutputExists() {
    final String outputBucketName = "output";
    final JobConfig jobConfig = new JobConfig(outputBucketName, 2019, 9, "filterSqlFile.sql");
    final Either<ValidationFailure, JobConfig> jobConfigOrFailure = Either.right(jobConfig);

    when(mockAmazonS3.doesBucketExistV2(outputBucketName)).thenReturn(true);

    jobConfigOutputValidator.validate(jobConfigOrFailure);

    verify(mockAmazonS3, times(1)).doesBucketExistV2(outputBucketName);
  }

  @Test
  public void whenOutputNotSuppliedThenReturnsUnvalidatedResult() {
    SoftAssertions.assertSoftly(
        softly -> {
          final JobConfig jobConfig = new JobConfig(null, 2019, 9, "filterSqlFile.sql");
          final Either<ValidationFailure, JobConfig> jobConfigOrFailure = Either.right(jobConfig);

          final Validated<ValidationFailure, JobConfig> unvalidatedResult =
              jobConfigOutputValidator.validate(jobConfigOrFailure);

          softly.assertThat(unvalidatedResult.isInvalid()).isTrue();
          softly
              .assertThat(unvalidatedResult.toEither().leftOrElse(null))
              .containsExactlyInAnyOrderElementsOf(
                  NonEmptyList.of(ValidationFailure.JOB_CONFIG_DOES_NOT_HAVE_OUTPUT));
        });
  }

  @Test
  public void whenS3DoesNotHaveBucketWithNameThenReturnsUnvalidatedResult() {
    SoftAssertions.assertSoftly(
        softly -> {
          final String outputBucketName = "output";
          final JobConfig jobConfig = new JobConfig(outputBucketName, 2019, 9, "filterSqlFile.sql");
          final Either<ValidationFailure, JobConfig> jobConfigOrFailure = Either.right(jobConfig);

          when(mockAmazonS3.doesBucketExistV2(outputBucketName)).thenReturn(false);

          final Validated<ValidationFailure, JobConfig> unvalidatedResult =
              jobConfigOutputValidator.validate(jobConfigOrFailure);

          softly.assertThat(unvalidatedResult.isInvalid()).isTrue();
          softly
              .assertThat(unvalidatedResult.toEither().leftOrElse(null))
              .containsExactlyInAnyOrderElementsOf(
                  NonEmptyList.of(ValidationFailure.OUTPUT_S3_BUCKET_DOES_NOT_EXIST));
        });
  }

  @Test
  public void whenQueryingS3FailsThenReturnsUnvalidatedResult() {
    SoftAssertions.assertSoftly(
        softly -> {
          final String outputBucketName = "output";
          final JobConfig jobConfig = new JobConfig(outputBucketName, 2019, 9, "filterSqlFile.sql");
          final Either<ValidationFailure, JobConfig> jobConfigOrFailure = Either.right(jobConfig);

          when(mockAmazonS3.doesBucketExistV2(outputBucketName))
              .thenThrow(new AmazonS3Exception("Unauthorized"));

          final Validated<ValidationFailure, JobConfig> unvalidatedResult =
              jobConfigOutputValidator.validate(jobConfigOrFailure);

          softly.assertThat(unvalidatedResult.isInvalid()).isTrue();
          softly
              .assertThat(unvalidatedResult.toEither().leftOrElse(null))
              .containsExactlyInAnyOrderElementsOf(
                  NonEmptyList.of(ValidationFailure.OUTPUT_S3_BUCKET_DOES_NOT_EXIST));
        });
  }
}
