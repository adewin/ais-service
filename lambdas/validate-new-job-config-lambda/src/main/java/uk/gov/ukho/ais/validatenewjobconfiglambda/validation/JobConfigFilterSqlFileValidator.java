package uk.gov.ukho.ais.validatenewjobconfiglambda.validation;

import cyclops.control.Either;
import cyclops.control.Option;
import cyclops.control.Validated;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.ukho.ais.lambda.heatmap.job.model.JobConfig;
import uk.gov.ukho.ais.lambda.heatmap.job.model.validation.ValidationFailure;
import uk.gov.ukho.ais.validatenewjobconfiglambda.repository.FilterSqlFileRepository;

@Component
public class JobConfigFilterSqlFileValidator implements Validator {

  private final FilterSqlFileRepository filterSqlFileRepository;

  public JobConfigFilterSqlFileValidator(FilterSqlFileRepository filterSqlFileRepository) {
    this.filterSqlFileRepository = filterSqlFileRepository;
  }

  @Override
  public Validated<ValidationFailure, JobConfig> validate(
      final Either<ValidationFailure, JobConfig> jobConfigOrFailure) {
    return Validator.isNotEmpty(jobConfigOrFailure, JobConfig::getFilterSqlFile)
        .apply(ValidationFailure.JOB_CONFIG_DOES_NOT_HAVE_FILTER_SQL_FILE)
        .combine(RETURN_FIRST, filterSqlFileExists(jobConfigOrFailure));
  }

  private Validated<ValidationFailure, JobConfig> filterSqlFileExists(
      final Either<ValidationFailure, JobConfig> jobConfigOrFailure) {

    return jobConfigOrFailure
        .fold(l -> Option.<JobConfig>none(), Option::of)
        .map(JobConfig::getFilterSqlFile)
        .filter(StringUtils::isNoneBlank)
        .map(filterSqlFileRepository::exists)
        .filter(exists -> !exists)
        .map(v -> Validator.invalid(ValidationFailure.FILTER_SQL_FILE_DOES_NOT_EXIST))
        .orElse(Validator.valid(jobConfigOrFailure));
  }
}
