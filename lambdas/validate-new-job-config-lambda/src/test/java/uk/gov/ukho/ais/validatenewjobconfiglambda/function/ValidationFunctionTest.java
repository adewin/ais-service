package uk.gov.ukho.ais.validatenewjobconfiglambda.function;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

import cyclops.control.Either;
import cyclops.control.Option;
import cyclops.control.Validated;
import java.util.Collections;
import org.assertj.core.api.SoftAssertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.ukho.ais.lambda.heatmap.job.model.JobConfig;
import uk.gov.ukho.ais.lambda.heatmap.job.model.StepFunctionInput;
import uk.gov.ukho.ais.lambda.heatmap.job.model.validation.ValidationFailure;
import uk.gov.ukho.ais.lambda.heatmap.job.model.validation.ValidationResult;
import uk.gov.ukho.ais.validatenewjobconfiglambda.repository.JobConfigRepository;
import uk.gov.ukho.ais.validatenewjobconfiglambda.validation.JobConfigValidationService;
import uk.gov.ukho.ais.validatenewjobconfiglambda.validation.ValidationResultFactory;

@RunWith(MockitoJUnitRunner.class)
public class ValidationFunctionTest {

  @Mock private JobConfigRepository mockJobConfigRepository;

  @Mock private JobConfigValidationService mockJobConfigValidationService;

  @Mock private ValidationResultFactory mockValidationResultFactory;

  @InjectMocks private ValidationFunction validationFunction;

  @Test
  public void whenJobConfigExistsThenRequestedJobConfigReturned() {
    SoftAssertions.assertSoftly(
        softly -> {
          final String jobConfigFile = "s3://bucket/job-config.json";
          final JobConfig jobConfig = new JobConfig("output", 2019, 1, "filter.sql");
          final ValidationResult validationResult =
              new ValidationResult(
                  Option.of(jobConfig.withFilterSqlFile("s3://test-bucket/filter.sql")),
                  Collections.emptyList());
          final Either<ValidationFailure, JobConfig> jobConfigInRepo = Either.right(jobConfig);

          when(mockJobConfigRepository.getJobConfig(jobConfigFile)).thenReturn(jobConfigInRepo);
          when(mockJobConfigValidationService.performValidation(jobConfigInRepo))
              .thenReturn(Validated.valid(jobConfig));
          when(mockValidationResultFactory.valid(jobConfig)).thenReturn(validationResult);

          final ValidationResult result =
              validationFunction.apply(new StepFunctionInput(jobConfigFile));

          softly.assertThat(result).isEqualToComparingFieldByFieldRecursively(validationResult);
        });
  }

  @Test
  public void whenJobConfigSuppliedThenItIsRetrievedFromJobConfigRepo() {
    final String jobConfigFile = "s3://bucket/job-config.json";
    final JobConfig jobConfig = new JobConfig("output", 2019, 1, "filter.sql");

    final Either<ValidationFailure, JobConfig> jobConfigInRepo = Either.right(jobConfig);

    when(mockJobConfigRepository.getJobConfig(jobConfigFile)).thenReturn(jobConfigInRepo);
    when(mockJobConfigValidationService.performValidation(jobConfigInRepo))
        .thenReturn(Validated.valid(jobConfig));

    validationFunction.apply(new StepFunctionInput(jobConfigFile));

    verify(mockJobConfigRepository, times(1)).getJobConfig(jobConfigFile);
  }

  @Test
  public void whenJobConfigExistsThenItHasBeenValidated() {
    final String jobConfigFile = "s3://bucket/job-config.json";
    final JobConfig jobConfig = new JobConfig("output", 2019, 1, "filter.sql");
    final Either<ValidationFailure, JobConfig> jobConfigInRepo = Either.right(jobConfig);

    when(mockJobConfigRepository.getJobConfig(jobConfigFile)).thenReturn(jobConfigInRepo);
    when(mockJobConfigValidationService.performValidation(jobConfigInRepo))
        .thenReturn(Validated.valid(jobConfig));

    validationFunction.apply(new StepFunctionInput(jobConfigFile));

    verify(mockJobConfigValidationService, times(1)).performValidation(jobConfigInRepo);
  }
}
