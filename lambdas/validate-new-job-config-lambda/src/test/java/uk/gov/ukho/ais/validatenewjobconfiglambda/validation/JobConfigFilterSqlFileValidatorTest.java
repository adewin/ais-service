package uk.gov.ukho.ais.validatenewjobconfiglambda.validation;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

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
import uk.gov.ukho.ais.validatenewjobconfiglambda.repository.FilterSqlFileRepository;

@RunWith(MockitoJUnitRunner.class)
public class JobConfigFilterSqlFileValidatorTest {
  @Mock private FilterSqlFileRepository mockFilterSqlFileRepository;

  @InjectMocks private JobConfigFilterSqlFileValidator jobConfigFilterSqlFileValidator;

  private final String filterSqlFileName = "filterSqlFile.sql";

  @Test
  public void whenSqlFileSuppliedAndExistsThenReturnsValidatedResult() {
    SoftAssertions.assertSoftly(
        softly -> {
          final JobConfig jobConfig = new JobConfig("output", "2019", "9", filterSqlFileName);
          final Either<ValidationFailure, JobConfig> jobConfigOrFailure = Either.right(jobConfig);

          when(mockFilterSqlFileRepository.exists(filterSqlFileName)).thenReturn(true);

          final Validated<ValidationFailure, JobConfig> validated =
              jobConfigFilterSqlFileValidator.validate(jobConfigOrFailure);

          softly.assertThat(validated.isValid()).isTrue();
          softly.assertThat(validated.orElse(null)).isEqualToComparingFieldByField(jobConfig);
        });
  }

  @Test
  public void whenSqlFileSuppliedThenRepositoryCheckedToSeeIfFilterFileExists() {
    final JobConfig jobConfig = new JobConfig("output", "2019", "9", filterSqlFileName);
    final Either<ValidationFailure, JobConfig> jobConfigOrFailure = Either.right(jobConfig);

    when(mockFilterSqlFileRepository.exists(filterSqlFileName)).thenReturn(true);

    jobConfigFilterSqlFileValidator.validate(jobConfigOrFailure);

    verify(mockFilterSqlFileRepository, times(1)).exists(filterSqlFileName);
  }

  @Test
  public void whenNoSqlFileSuppliedAndExistsThenReturnsUnvalidatedResult() {
    SoftAssertions.assertSoftly(
        softly -> {
          final JobConfig jobConfig = new JobConfig("output", "2019", "9", null);
          final Either<ValidationFailure, JobConfig> jobConfigOrFailure = Either.right(jobConfig);

          final Validated<ValidationFailure, JobConfig> unvalidatedResult =
              jobConfigFilterSqlFileValidator.validate(jobConfigOrFailure);

          softly.assertThat(unvalidatedResult.isInvalid()).isTrue();
          softly
              .assertThat(unvalidatedResult.toEither().leftOrElse(null))
              .containsExactlyInAnyOrderElementsOf(
                  NonEmptyList.of(ValidationFailure.JOB_CONFIG_DOES_NOT_HAVE_FILTER_SQL_FILE));
        });
  }

  @Test
  public void whenBlankSqlFileSuppliedAndExistsThenReturnsUnvalidatedResult() {
    SoftAssertions.assertSoftly(
        softly -> {
          final JobConfig jobConfig = new JobConfig("output", "2019", "9", "");
          final Either<ValidationFailure, JobConfig> jobConfigOrFailure = Either.right(jobConfig);

          final Validated<ValidationFailure, JobConfig> unvalidatedResult =
              jobConfigFilterSqlFileValidator.validate(jobConfigOrFailure);

          softly.assertThat(unvalidatedResult.isInvalid()).isTrue();
          softly
              .assertThat(unvalidatedResult.toEither().leftOrElse(null))
              .containsExactlyInAnyOrderElementsOf(
                  NonEmptyList.of(ValidationFailure.JOB_CONFIG_DOES_NOT_HAVE_FILTER_SQL_FILE));
        });
  }

  @Test
  public void whenSqlFileSuppliedWhichDoesNotExistThenReturnsUnvalidatedResult() {
    SoftAssertions.assertSoftly(
        softly -> {
          final JobConfig jobConfig = new JobConfig("output", "2019", "9", filterSqlFileName);
          final Either<ValidationFailure, JobConfig> jobConfigOrFailure = Either.right(jobConfig);

          when(mockFilterSqlFileRepository.exists(filterSqlFileName)).thenReturn(false);

          final Validated<ValidationFailure, JobConfig> unvalidatedResult =
              jobConfigFilterSqlFileValidator.validate(jobConfigOrFailure);

          softly.assertThat(unvalidatedResult.isInvalid()).isTrue();
          softly
              .assertThat(unvalidatedResult.toEither().leftOrElse(null))
              .containsExactlyInAnyOrderElementsOf(
                  NonEmptyList.of(ValidationFailure.FILTER_SQL_FILE_DOES_NOT_EXIST));
        });
  }
}
