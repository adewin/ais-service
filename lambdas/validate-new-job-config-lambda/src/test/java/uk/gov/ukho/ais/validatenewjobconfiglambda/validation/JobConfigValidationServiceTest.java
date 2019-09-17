package uk.gov.ukho.ais.validatenewjobconfiglambda.validation;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

import cyclops.control.Either;
import cyclops.control.Validated;
import java.util.Arrays;
import org.assertj.core.api.SoftAssertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.ukho.ais.lambda.heatmap.job.model.JobConfig;
import uk.gov.ukho.ais.lambda.heatmap.job.model.validation.ValidationFailure;
import uk.gov.ukho.ais.validatenewjobconfiglambda.repository.FilterSqlFileRepository;

@RunWith(MockitoJUnitRunner.class)
public class JobConfigValidationServiceTest {

  @Mock private FilterSqlFileRepository mockFilterSqlFileRepository;

  @Mock private Validator mockValidator1;

  @Mock private Validator mockValidator2;

  private JobConfigValidationService jobConfigValidationService;

  @Before
  public void setUp() {
    jobConfigValidationService =
        new JobConfigValidationService(Arrays.asList(mockValidator1, mockValidator2));
  }

  @Test
  public void whenJobConfigIsValidThenReturnsValidResult() {
    SoftAssertions.assertSoftly(
        softly -> {
          final String filterSqlFile = "filterSqlFile.sql";
          final JobConfig validJobConfig = new JobConfig("output", 2019, 9, filterSqlFile);

          final Either<ValidationFailure, JobConfig> jobConfigOrFailure =
              Either.right(validJobConfig);

          when(mockValidator1.validate(jobConfigOrFailure))
              .thenReturn(Validated.valid(validJobConfig));
          when(mockValidator2.validate(jobConfigOrFailure))
              .thenReturn(Validated.valid(validJobConfig));

          final Validated<ValidationFailure, JobConfig> validated =
              jobConfigValidationService.performValidation(jobConfigOrFailure);

          softly.assertThat(validated.isValid()).isTrue();
          softly.assertThat(validated.orElse(null)).isEqualToComparingFieldByField(validJobConfig);
        });
  }

  @Test
  public void whenJobConfigIsValidThenAllValidatorsHaveValidatedTheJobConfig() {

    final String filterSqlFile = "filterSqlFile.sql";
    final JobConfig validJobConfig = new JobConfig("output", 2019, 9, filterSqlFile);

    final Either<ValidationFailure, JobConfig> jobConfigOrFailure = Either.right(validJobConfig);

    when(mockValidator1.validate(jobConfigOrFailure)).thenReturn(Validated.valid(validJobConfig));
    when(mockValidator2.validate(jobConfigOrFailure)).thenReturn(Validated.valid(validJobConfig));

    jobConfigValidationService.performValidation(jobConfigOrFailure);

    verify(mockValidator1, times(1)).validate(jobConfigOrFailure);
    verify(mockValidator2, times(1)).validate(jobConfigOrFailure);
  }

  @Test
  public void whenConfigDoesNotExistThenReturnsAnUnvalidatedResult() {
    SoftAssertions.assertSoftly(
        softly -> {
          final Either<ValidationFailure, JobConfig> failure =
              Either.left(ValidationFailure.JOB_CONFIG_NOT_VALID_JSON);

          when(mockValidator1.validate(failure)).thenReturn(Validated.valid(JobConfig.empty()));
          when(mockValidator2.validate(failure)).thenReturn(Validated.valid(JobConfig.empty()));

          final Validated<ValidationFailure, JobConfig> unvalidatedResult =
              jobConfigValidationService.performValidation(failure);

          softly.assertThat(unvalidatedResult.isInvalid()).isTrue();
          softly
              .assertThat(unvalidatedResult.toEither().leftOrElse(null))
              .containsExactly(ValidationFailure.JOB_CONFIG_NOT_VALID_JSON);
        });
  }

  @Test
  public void whenOneValidatorFailsThenReturnsUnvalidatedResult() {
    SoftAssertions.assertSoftly(
        softly -> {
          final String filterSqlFile = "filterSqlFile.sql";
          final JobConfig validJobConfig = new JobConfig("output", 2019, 9, filterSqlFile);

          final Either<ValidationFailure, JobConfig> jobConfigOrFailure =
              Either.right(validJobConfig);

          when(mockValidator1.validate(jobConfigOrFailure))
              .thenReturn(Validated.invalid(ValidationFailure.FILTER_SQL_FILE_DOES_NOT_EXIST));
          when(mockValidator2.validate(jobConfigOrFailure))
              .thenReturn(Validated.valid(JobConfig.empty()));

          final Validated<ValidationFailure, JobConfig> unvalidatedResult =
              jobConfigValidationService.performValidation(jobConfigOrFailure);

          softly.assertThat(unvalidatedResult.isInvalid()).isTrue();
          softly
              .assertThat(unvalidatedResult.toEither().leftOrElse(null))
              .containsExactly(ValidationFailure.FILTER_SQL_FILE_DOES_NOT_EXIST);
        });
  }

  @Test
  public void whenMultipleValidatorsFailsThenReturnsAllUnvalidatedResults() {
    SoftAssertions.assertSoftly(
        softly -> {
          final String filterSqlFile = "filterSqlFile.sql";
          final JobConfig validJobConfig = new JobConfig("output", 2019, 9, filterSqlFile);

          final Either<ValidationFailure, JobConfig> jobConfigOrFailure =
              Either.right(validJobConfig);

          when(mockValidator1.validate(jobConfigOrFailure))
              .thenReturn(Validated.invalid(ValidationFailure.FILTER_SQL_FILE_DOES_NOT_EXIST));
          when(mockValidator2.validate(jobConfigOrFailure))
              .thenReturn(
                  Validated.invalid(ValidationFailure.JOB_CONFIG_DOES_NOT_HAVE_FILTER_SQL_FILE));

          final Validated<ValidationFailure, JobConfig> unvalidatedResult =
              jobConfigValidationService.performValidation(jobConfigOrFailure);

          softly.assertThat(unvalidatedResult.isInvalid()).isTrue();
          softly
              .assertThat(unvalidatedResult.toEither().leftOrElse(null))
              .containsExactlyInAnyOrder(
                  ValidationFailure.FILTER_SQL_FILE_DOES_NOT_EXIST,
                  ValidationFailure.JOB_CONFIG_DOES_NOT_HAVE_FILTER_SQL_FILE);
        });
  }
}
