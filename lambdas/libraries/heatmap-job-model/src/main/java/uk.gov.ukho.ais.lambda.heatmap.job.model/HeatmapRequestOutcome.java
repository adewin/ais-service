package uk.gov.ukho.ais.lambda.heatmap.job.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import cyclops.control.Option;
import uk.gov.ukho.ais.lambda.heatmap.job.model.validation.ValidationResult;

public class HeatmapRequestOutcome {

  private final String jobConfigFile;
  private final String executionId;
  private final Object generateHeatmaps;
  private final Option<ValidationResult> validationResult;
  private final Option<Object> validationFailure;
  private final Option<Object> heatmapGenerationFailure;
  private final Option<Object> heatmapAggregationFailure;
  private final Option<Object> heatmapAggregation;

  @JsonCreator
  public HeatmapRequestOutcome(
      @JsonProperty("executionId") final String executionId,
      @JsonProperty("jobConfigFile") final String jobConfigFile,
      @JsonProperty("validationResult") final ValidationResult validationResult,
      @JsonProperty("generateHeatmaps") final Object generateHeatmaps,
      @JsonProperty("heatmapAggregation") final Object heatmapAggregation,
      @JsonProperty("validationFailure") final Object validationFailure,
      @JsonProperty("heatmapGenerationFailure") final Object heatmapGenerationFailure,
      @JsonProperty("heatmapAggregationFailure") final Object heatmapAggregationFailure) {
    this.jobConfigFile = jobConfigFile;
    this.executionId = executionId;
    this.generateHeatmaps = generateHeatmaps;
    this.validationResult = Option.ofNullable(validationResult);
    this.validationFailure = Option.ofNullable(validationFailure);
    this.heatmapGenerationFailure = Option.ofNullable(heatmapGenerationFailure);
    this.heatmapAggregationFailure = Option.ofNullable(heatmapAggregationFailure);
    this.heatmapAggregation = Option.ofNullable(heatmapAggregation);
  }

  public String getJobConfigFile() {
    return jobConfigFile;
  }

  public Option<ValidationResult> getValidationResult() {
    return validationResult;
  }

  public Option<Object> getValidationFailure() {
    return validationFailure;
  }

  public Option<Object> getHeatmapGenerationFailure() {
    return heatmapGenerationFailure;
  }

  public Object getGenerateHeatmaps() {
    return generateHeatmaps;
  }

  public Option<Object> getHeatmapAggregationFailure() {
    return heatmapAggregationFailure;
  }

  public Option<Object> getHeatmapAggregation() {
    return heatmapAggregation;
  }

  public String getExecutionId() {
    return executionId;
  }
}
