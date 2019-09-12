package uk.gov.ukho.ais.validatenewjobconfiglambda.validation;

import cyclops.control.Either;
import cyclops.control.Option;
import cyclops.control.Validated;
import cyclops.function.Semigroup;
import java.util.Objects;
import java.util.function.Function;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import uk.gov.ukho.ais.validatenewjobconfiglambda.model.JobConfig;
import uk.gov.ukho.ais.validatenewjobconfiglambda.model.ValidationFailure;

public interface Validator {

  Validated<ValidationFailure, JobConfig> validate(
      final Either<ValidationFailure, JobConfig> jobConfigOrFailure);

  Semigroup<JobConfig> RETURN_FIRST = (x, y) -> x;

  static Function<ValidationFailure, Validated<ValidationFailure, JobConfig>> intWithinBounds(
      final Either<ValidationFailure, JobConfig> jobConfigOrFailure,
      final Function<JobConfig, Integer> extractorFunction,
      final Pair<Integer, Integer> minMaxBounds) {
    return validationFailure ->
        jobConfigOrFailure
            .fold(l -> Option.<JobConfig>none(), Option::of)
            .map(extractorFunction)
            .filter(Objects::nonNull)
            .filter(v -> v < minMaxBounds.getLeft() || v > minMaxBounds.getRight())
            .map(m -> invalid(validationFailure))
            .orElse(valid(jobConfigOrFailure));
  }

  static Function<ValidationFailure, Validated<ValidationFailure, JobConfig>> isNotEmpty(
      Either<ValidationFailure, JobConfig> jobConfigOrFailure,
      Function<JobConfig, String> extractorFunction) {
    return validationFailure ->
        jobConfigOrFailure
            .fold(l -> Option.<JobConfig>none(), Option::of)
            .map(extractorFunction)
            .filter(StringUtils::isAllBlank)
            .map(v -> invalid(validationFailure))
            .orElse(valid(jobConfigOrFailure));
  }

  static <T> Function<ValidationFailure, Validated<ValidationFailure, JobConfig>> isNotNull(
      Either<ValidationFailure, JobConfig> jobConfigOrFailure,
      final Function<JobConfig, T> extractorFunction) {
    return validationFailure ->
        jobConfigOrFailure
            .fold(l -> Option.<JobConfig>none(), Option::of)
            .map(extractorFunction)
            .filter(Objects::isNull)
            .map(v -> invalid(validationFailure))
            .orElse(valid(jobConfigOrFailure));
  }

  static Validated<ValidationFailure, JobConfig> valid(
      Either<ValidationFailure, JobConfig> jobConfigOrFailure) {
    return jobConfigOrFailure
        .map(Validated::<ValidationFailure, JobConfig>valid)
        .orElse(Validated.valid(null));
  }

  static Validated<ValidationFailure, JobConfig> invalid(ValidationFailure validationFailure) {
    return Validated.invalid(validationFailure);
  }
}
