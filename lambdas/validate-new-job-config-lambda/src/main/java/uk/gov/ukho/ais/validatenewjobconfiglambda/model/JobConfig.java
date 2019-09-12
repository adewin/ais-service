package uk.gov.ukho.ais.validatenewjobconfiglambda.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class JobConfig {

  private final String output;
  private final Integer year;
  private final Integer month;
  private final String filterSqlFile;

  @JsonCreator
  public JobConfig(
      @JsonProperty("output") final String output,
      @JsonProperty("year") final Integer year,
      @JsonProperty("month") final Integer month,
      @JsonProperty("filterSqlFile") final String filterSqlFile) {
    this.output = output;
    this.year = year;
    this.month = month;
    this.filterSqlFile = filterSqlFile;
  }

  public String getOutput() {
    return output;
  }

  public Integer getYear() {
    return year;
  }

  public Integer getMonth() {
    return month;
  }

  public String getFilterSqlFile() {
    return filterSqlFile;
  }

  public JobConfig withFilterSqlFile(final String filterSqlFile) {
    return new JobConfig(output, year, month, filterSqlFile);
  }

  public static JobConfig empty() {
    return new JobConfig("", 0, 0, "");
  }
}
