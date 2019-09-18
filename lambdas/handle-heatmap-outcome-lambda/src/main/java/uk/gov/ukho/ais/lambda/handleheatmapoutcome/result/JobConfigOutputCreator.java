package uk.gov.ukho.ais.lambda.handleheatmapoutcome.result;

import org.apache.commons.io.FilenameUtils;
import uk.gov.ukho.ais.lambda.heatmap.job.model.HeatmapRequestOutcome;
import uk.gov.ukho.ais.lambda.heatmap.job.model.JobConfig;
import uk.gov.ukho.ais.lambda.heatmap.job.model.validation.ValidationResult;

class JobConfigOutputCreator {

  static JobConfig createOutputJobConfig(final HeatmapRequestOutcome heatmapRequestOutcome) {
    return heatmapRequestOutcome
        .getJobConfig()
        .map(ValidationResult::getJobConfig)
        .map(jobConfig -> jobConfig.withFilterSqlFile(sqlFileName(jobConfig.getFilterSqlFile())))
        .orElse(null);
  }

  private static String sqlFileName(final String fullyQualifiedSqlFilterFile) {
    return FilenameUtils.getName(fullyQualifiedSqlFilterFile);
  }
}
