package uk.gov.ukho.ais.validatenewjobconfiglambda.validation;

import cyclops.control.Either;
import cyclops.control.Validated;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;
import uk.gov.ukho.ais.validatenewjobconfiglambda.model.JobConfig;
import uk.gov.ukho.ais.validatenewjobconfiglambda.model.ValidationFailure;

@Component
class JobConfigYearValidator implements Validator {

  @Override
  public Validated<ValidationFailure, JobConfig> validate(
      final Either<ValidationFailure, JobConfig> jobConfigOrFailure) {
    final Validated<ValidationFailure, JobConfig> isNotNull =
        Validator.isNotNull(jobConfigOrFailure, JobConfig::getYear)
            .apply(ValidationFailure.JOB_CONFIG_DOES_NOT_HAVE_YEAR);

    final Validated<ValidationFailure, JobConfig> isWithinSensibleYearRange =
        Validator.intWithinBounds(jobConfigOrFailure, JobConfig::getYear, Pair.of(1970, 3000))
            .apply(ValidationFailure.JOB_CONFIG_CONTAINS_INVALID_YEAR);

    return isNotNull.combine(Validator.RETURN_FIRST, isWithinSensibleYearRange);
  }
}
