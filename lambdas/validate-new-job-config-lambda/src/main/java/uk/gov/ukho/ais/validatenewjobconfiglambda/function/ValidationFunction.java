package uk.gov.ukho.ais.validatenewjobconfiglambda.function;

import cyclops.control.Either;
import java.util.function.Function;
import org.springframework.stereotype.Component;
import uk.gov.ukho.ais.validatenewjobconfiglambda.model.JobConfig;
import uk.gov.ukho.ais.validatenewjobconfiglambda.model.ValidationFailure;
import uk.gov.ukho.ais.validatenewjobconfiglambda.model.ValidationRequest;
import uk.gov.ukho.ais.validatenewjobconfiglambda.model.ValidationResult;
import uk.gov.ukho.ais.validatenewjobconfiglambda.repository.JobConfigRepository;
import uk.gov.ukho.ais.validatenewjobconfiglambda.validation.JobConfigValidationService;
import uk.gov.ukho.ais.validatenewjobconfiglambda.validation.ValidationResultFactory;

@Component
public class ValidationFunction implements Function<ValidationRequest, ValidationResult> {

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
  public ValidationResult apply(ValidationRequest validationRequest) {
    final Either<ValidationFailure, JobConfig> jobConfig =
        jobConfigRepository.getJobConfig(validationRequest.getJobConfigFile());
    return jobConfigValidationService
        .performValidation(jobConfig)
        .fold(validationResultFactory::invalid, validationResultFactory::valid);
  }
}
