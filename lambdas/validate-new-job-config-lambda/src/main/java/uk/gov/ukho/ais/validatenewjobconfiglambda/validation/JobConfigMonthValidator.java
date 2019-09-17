package uk.gov.ukho.ais.validatenewjobconfiglambda.validation;

import cyclops.control.Either;
import cyclops.control.Validated;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;
import uk.gov.ukho.ais.lambda.heatmap.job.model.JobConfig;
import uk.gov.ukho.ais.lambda.heatmap.job.model.validation.ValidationFailure;

@Component
public class JobConfigMonthValidator implements Validator {

  @Override
  public Validated<ValidationFailure, JobConfig> validate(
      Either<ValidationFailure, JobConfig> jobConfigOrFailure) {
    final Validated<ValidationFailure, JobConfig> isNotNull =
        Validator.isNotNull(jobConfigOrFailure, JobConfig::getMonth)
            .apply(ValidationFailure.JOB_CONFIG_DOES_NOT_HAVE_MONTH);

    final Validated<ValidationFailure, JobConfig> isWithinMonthBounds =
        Validator.intWithinBounds(jobConfigOrFailure, JobConfig::getMonth, Pair.of(1, 12))
            .apply(ValidationFailure.JOB_CONFIG_CONTAINS_INVALID_MONTH);
    return isNotNull.combine(Validator.RETURN_FIRST, isWithinMonthBounds);
  }
}
