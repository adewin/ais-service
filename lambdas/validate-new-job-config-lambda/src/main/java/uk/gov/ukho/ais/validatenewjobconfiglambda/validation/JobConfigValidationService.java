package uk.gov.ukho.ais.validatenewjobconfiglambda.validation;

import cyclops.control.Either;
import cyclops.control.Validated;
import java.util.List;
import org.springframework.stereotype.Component;
import uk.gov.ukho.ais.lambda.heatmap.job.model.JobConfig;
import uk.gov.ukho.ais.lambda.heatmap.job.model.validation.ValidationFailure;

@Component
public class JobConfigValidationService {

  private final List<Validator> validators;

  public JobConfigValidationService(final List<Validator> validators) {
    this.validators = validators;
  }

  public Validated<ValidationFailure, JobConfig> performValidation(
      final Either<ValidationFailure, JobConfig> jobConfigOrFailure) {

    return this.validators.stream()
        .map(validator -> validator.validate(jobConfigOrFailure))
        .reduce(
            jobConfigExists(jobConfigOrFailure), (a, b) -> a.combine(Validator.RETURN_FIRST, b));
  }

  private Validated<ValidationFailure, JobConfig> jobConfigExists(
      Either<ValidationFailure, JobConfig> jobConfigOrFailure) {
    return jobConfigOrFailure.fold(Validator::invalid, Validated::valid);
  }
}
