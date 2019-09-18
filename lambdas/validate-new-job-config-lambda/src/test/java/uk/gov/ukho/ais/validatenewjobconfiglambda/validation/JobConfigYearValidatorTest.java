package uk.gov.ukho.ais.validatenewjobconfiglambda.validation;

import cyclops.control.Either;
import cyclops.control.Validated;
import cyclops.data.NonEmptyList;
import org.assertj.core.api.SoftAssertions;
import org.junit.Test;
import uk.gov.ukho.ais.lambda.heatmap.job.model.JobConfig;
import uk.gov.ukho.ais.lambda.heatmap.job.model.validation.ValidationFailure;

public class JobConfigYearValidatorTest {

  private JobConfigYearValidator jobConfigYearValidator = new JobConfigYearValidator();

  @Test
  public void whenConfigSuppliesValidYearThenReturnsValidatedResult() {

    SoftAssertions.assertSoftly(
        softly -> {
          final JobConfig jobConfig = new JobConfig("output", "2019", "1", "filterSqlFile.sql");
          final Either<ValidationFailure, JobConfig> jobConfigOrFailure = Either.right(jobConfig);

          final Validated<ValidationFailure, JobConfig> validated =
              jobConfigYearValidator.validate(jobConfigOrFailure);

          softly.assertThat(validated.isValid()).isTrue();
          softly.assertThat(validated.orElse(null)).isEqualToComparingFieldByField(jobConfig);
        });
  }

  @Test
  public void whenConfigDoesNotSpecifyYearThenReturnsUnvalidatedResult() {
    SoftAssertions.assertSoftly(
        softly -> {
          final JobConfig jobConfig = new JobConfig("output", null, "1", "filterSqlFile.sql");
          final Either<ValidationFailure, JobConfig> jobConfigOrFailure = Either.right(jobConfig);

          final Validated<ValidationFailure, JobConfig> unvalidatedResult =
              jobConfigYearValidator.validate(jobConfigOrFailure);

          softly.assertThat(unvalidatedResult.isInvalid()).isTrue();
          softly
              .assertThat(unvalidatedResult.toEither().leftOrElse(null))
              .containsExactlyInAnyOrderElementsOf(
                  NonEmptyList.of(ValidationFailure.JOB_CONFIG_DOES_NOT_HAVE_YEAR));
        });
  }

  @Test
  public void whenConfigSpecifiesInvalidYearThenReturnsUnvalidatedResult() {
    SoftAssertions.assertSoftly(
        softly -> {
          final JobConfig jobConfig = new JobConfig("output", "-2019", "1", "filterSqlFile.sql");
          final Either<ValidationFailure, JobConfig> jobConfigOrFailure = Either.right(jobConfig);

          final Validated<ValidationFailure, JobConfig> unvalidatedResult =
              jobConfigYearValidator.validate(jobConfigOrFailure);

          softly.assertThat(unvalidatedResult.isInvalid()).isTrue();
          softly
              .assertThat(unvalidatedResult.toEither().leftOrElse(null))
              .containsExactlyInAnyOrderElementsOf(
                  NonEmptyList.of(ValidationFailure.JOB_CONFIG_CONTAINS_INVALID_YEAR));
        });
  }

  @Test
  public void whenConfigSpecifiesNonNumericYearThenReturnsUnvalidatedResult() {
    SoftAssertions.assertSoftly(
        softly -> {
          final JobConfig jobConfig = new JobConfig("output", "INVALID", "1", "filterSqlFile.sql");
          final Either<ValidationFailure, JobConfig> jobConfigOrFailure = Either.right(jobConfig);

          final Validated<ValidationFailure, JobConfig> unvalidatedResult =
              jobConfigYearValidator.validate(jobConfigOrFailure);

          softly.assertThat(unvalidatedResult.isInvalid()).isTrue();
          softly
              .assertThat(unvalidatedResult.toEither().leftOrElse(null))
              .containsExactlyInAnyOrderElementsOf(
                  NonEmptyList.of(ValidationFailure.JOB_CONFIG_CONTAINS_INVALID_YEAR));
        });
  }
}
