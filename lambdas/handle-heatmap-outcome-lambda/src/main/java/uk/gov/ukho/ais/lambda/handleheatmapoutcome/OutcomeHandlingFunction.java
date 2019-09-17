package uk.gov.ukho.ais.lambda.handleheatmapoutcome;

import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import uk.gov.ukho.ais.lambda.handleheatmapoutcome.result.StepFunctionOutputFactory;
import uk.gov.ukho.ais.lambda.heatmap.job.model.HeatmapRequestOutcome;
import uk.gov.ukho.ais.lambda.heatmap.job.model.StepFunctionOutput;

@Component
public class OutcomeHandlingFunction
    implements Function<HeatmapRequestOutcome, StepFunctionOutput> {

  private static final Logger LOGGER = LoggerFactory.getLogger(OutcomeHandlingFunction.class);

  private final StepFunctionOutputFactory stepFunctionOutputFactory;
  private final JobConfigSubmissionRepository jobConfigSubmissionRepository;

  public OutcomeHandlingFunction(
      final StepFunctionOutputFactory stepFunctionOutputFactory,
      final JobConfigSubmissionRepository jobConfigSubmissionRepository) {
    this.stepFunctionOutputFactory = stepFunctionOutputFactory;
    this.jobConfigSubmissionRepository = jobConfigSubmissionRepository;
  }

  @Override
  public StepFunctionOutput apply(final HeatmapRequestOutcome heatmapRequestOutcome) {
    final StepFunctionOutput output =
        stepFunctionOutputFactory.createStepFunctionOutput(heatmapRequestOutcome);
    return jobConfigSubmissionRepository
        .writeOutput(output)
        .flatMap(jobConfigSubmissionRepository::removeProcessedJobConfig)
        .fold(removalRequest -> output, handleFailure(output));
  }

  private Function<Exception, StepFunctionOutput> handleFailure(
      final StepFunctionOutput defaultOutput) {
    return exception -> {
      LOGGER.error("Could not write out sucess", exception);
      return defaultOutput;
    };
  }
}
