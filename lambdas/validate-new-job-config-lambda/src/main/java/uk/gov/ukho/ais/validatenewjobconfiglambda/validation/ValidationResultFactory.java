package uk.gov.ukho.ais.validatenewjobconfiglambda.validation;

import cyclops.data.NonEmptyList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.function.Function;
import org.springframework.stereotype.Component;
import uk.gov.ukho.ais.lambda.heatmap.job.model.JobConfig;
import uk.gov.ukho.ais.lambda.heatmap.job.model.validation.ValidationFailure;
import uk.gov.ukho.ais.lambda.heatmap.job.model.validation.ValidationResult;
import uk.gov.ukho.ais.validatenewjobconfiglambda.repository.FilterSqlFileRepository;

@Component
public class ValidationResultFactory {

  private final FilterSqlFileRepository filterSqlFileRepository;

  public ValidationResultFactory(FilterSqlFileRepository filterSqlFileRepository) {
    this.filterSqlFileRepository = filterSqlFileRepository;
  }

  public ValidationResult valid(final JobConfig jobConfig) {
    return new ValidationResult(
        addFilterSqlFileS3UriToJobConfig(jobConfig), Collections.emptyList());
  }

  public Function<NonEmptyList<ValidationFailure>, ValidationResult> buildInvalidResult(
      final JobConfig jobConfig) {
    return validationFailures ->
        new ValidationResult(jobConfig, new ArrayList<>(validationFailures.toList()));
  }

  private JobConfig addFilterSqlFileS3UriToJobConfig(JobConfig jobConfig) {
    final String s3Uri = filterSqlFileRepository.getS3Uri(jobConfig.getFilterSqlFile());
    return jobConfig.withFilterSqlFile(s3Uri);
  }
}
