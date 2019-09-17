package uk.gov.ukho.ais.validatenewjobconfiglambda.function;

import cyclops.control.Either;
import java.util.function.Function;
import org.springframework.stereotype.Component;
import uk.gov.ukho.ais.lambda.heatmap.job.model.JobConfig;
import uk.gov.ukho.ais.lambda.heatmap.job.model.StepFunctionInput;
import uk.gov.ukho.ais.lambda.heatmap.job.model.validation.ValidationFailure;
import uk.gov.ukho.ais.lambda.heatmap.job.model.validation.ValidationResult;
import uk.gov.ukho.ais.validatenewjobconfiglambda.repository.JobConfigRepository;
import uk.gov.ukho.ais.validatenewjobconfiglambda.validation.JobConfigValidationService;
import uk.gov.ukho.ais.validatenewjobconfiglambda.validation.ValidationResultFactory;

@Component
public class ValidationFunction implements Function<StepFunctionInput, ValidationResult> {

  private final JobConfigRepository jobConfigRepository;

  private final JobConfigValidationService jobConfigValidationService;

  private final ValidationResultFactory validationResultFactory;

  public ValidationFunction(
      JobConfigRepository jobConfigRepository,
      JobConfigValidationService jobConfigValidationService,
      ValidationResultFactory validationResultFactory) {
    this.jobConfigRepository = jobConfigRepository;
    this.jobConfigValidationService = jobConfigValidationService;
    this.validationResultFactory = validationResultFactory;
  }

  @Override
  public ValidationResult apply(StepFunctionInput stepFunctionInput) {
    final Either<ValidationFailure, JobConfig> jobConfig =
        jobConfigRepository.getJobConfig(stepFunctionInput.getJobConfigFile());
    return jobConfigValidationService
        .performValidation(jobConfig)
        .fold(validationResultFactory::invalid, validationResultFactory::valid);
  }
}
