package uk.gov.ukho.ais.lambda.heatmap.job.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import cyclops.control.Option;
import uk.gov.ukho.ais.lambda.heatmap.job.model.validation.ValidationResult;

public class HeatmapRequestOutcome {

  private final String jobConfigFile;
  private final Option<ValidationResult> jobConfig;
  private final Option<Object> validationFailure;
  private final Option<Object> heatmapGenerationFailure;
  private final Option<Object> create6hr30kmHeatmap;
  private final Option<Object> create18hr100kmHeatmap;
  private final Option<Object> heatmapAggregationFailure;
  private final Option<Object> heatmapAggregation;

  @JsonCreator
  public HeatmapRequestOutcome(
      @JsonProperty("jobConfigFile") final String jobConfigFile,
      @JsonProperty("jobConfig") final ValidationResult jobConfig,
      @JsonProperty("create6hr30kmHeatmap") final Object create6hr30kmHeatmap,
      @JsonProperty("create18hr100kmHeatmap") final Object create18hr100kmHeatmap,
      @JsonProperty("heatmapAggregation") final Object heatmapAggregation,
      @JsonProperty("validationFailure") final Object validationFailure,
      @JsonProperty("heatmapGenerationFailure") final Object heatmapGenerationFailure,
      @JsonProperty("heatmapAggregationFailure") final Object heatmapAggregationFailure) {
    this.jobConfigFile = jobConfigFile;
    this.jobConfig = Option.ofNullable(jobConfig);
    this.validationFailure = Option.ofNullable(validationFailure);
    this.heatmapGenerationFailure = Option.ofNullable(heatmapGenerationFailure);
    this.create6hr30kmHeatmap = Option.ofNullable(create6hr30kmHeatmap);
    this.create18hr100kmHeatmap = Option.ofNullable(create18hr100kmHeatmap);
    this.heatmapAggregationFailure = Option.ofNullable(heatmapAggregationFailure);
    this.heatmapAggregation = Option.ofNullable(heatmapAggregation);
  }

  public String getJobConfigFile() {
    return jobConfigFile;
  }

  public Option<ValidationResult> getJobConfig() {
    return jobConfig;
  }

  public Option<Object> getValidationFailure() {
    return validationFailure;
  }

  public Option<Object> getHeatmapGenerationFailure() {
    return heatmapGenerationFailure;
  }

  public Option<Object> getCreate6hr30kmHeatmap() {
    return create6hr30kmHeatmap;
  }

  public Option<Object> getCreate18hr100kmHeatmap() {
    return create18hr100kmHeatmap;
  }

  public Option<Object> getHeatmapAggregationFailure() {
    return heatmapAggregationFailure;
  }

  public Option<Object> getHeatmapAggregation() {
    return heatmapAggregation;
  }
}
