package uk.gov.ukho.ais.validatenewjobconfiglambda.validation;

import cyclops.control.Either;
import cyclops.control.Validated;
import cyclops.data.NonEmptyList;
import org.assertj.core.api.SoftAssertions;
import org.junit.Test;
import uk.gov.ukho.ais.validatenewjobconfiglambda.model.JobConfig;
import uk.gov.ukho.ais.validatenewjobconfiglambda.model.ValidationFailure;

public class JobConfigMonthValidatorTest {

  private JobConfigMonthValidator jobConfigMonthValidator = new JobConfigMonthValidator();

  @Test
  public void whenConfigSpecifiesValidMonthThenReturnsValidatedResult() {
    SoftAssertions.assertSoftly(
        softly -> {
          final JobConfig jobConfig = new JobConfig("output", 2019, 9, "filterSqlFile.sql");
          final Either<ValidationFailure, JobConfig> jobConfigOrFailure = Either.right(jobConfig);

          final Validated<ValidationFailure, JobConfig> validated =
              jobConfigMonthValidator.validate(jobConfigOrFailure);

          softly.assertThat(validated.isValid()).isTrue();
          softly.assertThat(validated.orElse(null)).isEqualToComparingFieldByField(jobConfig);
        });
  }

  @Test
  public void whenConfigSpecifiesNullValueForMonthThenReturnsUnvalidatedResult() {
    SoftAssertions.assertSoftly(
        softly -> {
          final String filterSqlFile = "filterSqlFile.sql";
          final JobConfig jobConfig = new JobConfig("output", 2019, null, filterSqlFile);
          final Either<ValidationFailure, JobConfig> jobConfigOrFailure = Either.right(jobConfig);

          final Validated<ValidationFailure, JobConfig> unvalidatedResult =
              jobConfigMonthValidator.validate(jobConfigOrFailure);

          softly.assertThat(unvalidatedResult.isInvalid()).isTrue();
          softly
              .assertThat(unvalidatedResult.toEither().leftOrElse(null))
              .containsExactlyInAnyOrderElementsOf(
                  NonEmptyList.of(ValidationFailure.JOB_CONFIG_DOES_NOT_HAVE_MONTH));
        });
  }

  @Test
  public void whenConfigSpecifiesInvalidMonthThenReturnsUnvalidatedResult() {
    SoftAssertions.assertSoftly(
        softly -> {
          final String filterSqlFile = "filterSqlFile.sql";
          final JobConfig jobConfig = new JobConfig("output", 2019, 19, filterSqlFile);
          final Either<ValidationFailure, JobConfig> jobConfigOrFailure = Either.right(jobConfig);

          final Validated<ValidationFailure, JobConfig> unvalidatedResult =
              jobConfigMonthValidator.validate(jobConfigOrFailure);

          softly.assertThat(unvalidatedResult.isInvalid()).isTrue();
          softly
              .assertThat(unvalidatedResult.toEither().leftOrElse(null))
              .containsExactlyInAnyOrderElementsOf(
                  NonEmptyList.of(ValidationFailure.JOB_CONFIG_CONTAINS_INVALID_MONTH));
        });
  }
}
