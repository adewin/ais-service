package uk.gov.ukho.ais.validatenewjobconfiglambda.validation;

import cyclops.control.Option;
import cyclops.data.NonEmptyList;
import java.util.ArrayList;
import java.util.Collections;
import org.springframework.stereotype.Component;
import uk.gov.ukho.ais.validatenewjobconfiglambda.model.JobConfig;
import uk.gov.ukho.ais.validatenewjobconfiglambda.model.ValidationFailure;
import uk.gov.ukho.ais.validatenewjobconfiglambda.model.ValidationResult;
import uk.gov.ukho.ais.validatenewjobconfiglambda.repository.FilterSqlFileRepository;

@Component
public class ValidationResultFactory {

  private final FilterSqlFileRepository filterSqlFileRepository;

  public ValidationResultFactory(FilterSqlFileRepository filterSqlFileRepository) {
    this.filterSqlFileRepository = filterSqlFileRepository;
  }

  public ValidationResult valid(final JobConfig jobConfig) {
    return new ValidationResult(
        Option.of(addFilterSqlFileS3UriToJobConfig(jobConfig)), Collections.emptyList());
  }

  public ValidationResult invalid(final NonEmptyList<ValidationFailure> validationFailures) {
    return new ValidationResult(Option.none(), new ArrayList<>(validationFailures.toList()));
  }

  private JobConfig addFilterSqlFileS3UriToJobConfig(JobConfig jobConfig) {
    final String s3Uri = filterSqlFileRepository.getS3Uri(jobConfig.getFilterSqlFile());
    return jobConfig.withFilterSqlFile(s3Uri);
  }
}
