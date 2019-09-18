package uk.gov.ukho.ais.lambda.heatmap.job.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class JobConfig {

  private final String output;
  private final String year;
  private final String month;
  private final String filterSqlFile;

  @JsonCreator
  public JobConfig(
      @JsonProperty("output") final String output,
      @JsonProperty("year") final String year,
      @JsonProperty("month") final String month,
      @JsonProperty("filterSqlFile") final String filterSqlFile) {
    this.output = output;
    this.year = year;
    this.month = month;
    this.filterSqlFile = filterSqlFile;
  }

  public String getOutput() {
    return output;
  }

  public String getYear() {
    return year;
  }

  public String getMonth() {
    return month;
  }

  public String getFilterSqlFile() {
    return filterSqlFile;
  }

  public JobConfig withFilterSqlFile(final String filterSqlFile) {
    return new JobConfig(output, year, month, filterSqlFile);
  }

  public static JobConfig empty() {
    return new JobConfig("", "", "", "");
  }
}
