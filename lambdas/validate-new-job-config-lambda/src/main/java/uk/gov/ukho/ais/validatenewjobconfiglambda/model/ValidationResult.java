package uk.gov.ukho.ais.validatenewjobconfiglambda.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import cyclops.control.Option;
import java.util.List;

public class ValidationResult {

  private final Option<JobConfig> data;
  private final List<ValidationFailure> error;

  @JsonCreator
  public ValidationResult(
      @JsonProperty("data") final Option<JobConfig> data,
      @JsonProperty("error") final List<ValidationFailure> error) {
    this.data = data;
    this.error = error;
  }

  public Option<JobConfig> getData() {
    return data;
  }

  public List<ValidationFailure> getError() {
    return error;
  }
}