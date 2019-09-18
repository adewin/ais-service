package uk.gov.ukho.ais.lambda.handleheatmapoutcome.result;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Component;
import uk.gov.ukho.ais.lambda.heatmap.job.model.StepFunctionOutput;

@Component
public class StepFunctionS3ObjectKeyService {

  private final Clock clock;

  public StepFunctionS3ObjectKeyService(final Clock clock) {
    this.clock = clock;
  }

  public String createS3CompletedObjectKey(final StepFunctionOutput stepFunctionOutput) {
    final String inputFileName = FilenameUtils.getBaseName(stepFunctionOutput.getInputS3Uri());
    final String inputFileExtension =
        FilenameUtils.getExtension(stepFunctionOutput.getInputS3Uri());
    final String completionTime =
        LocalDateTime.now(clock).format(DateTimeFormatter.ofPattern("YYYY-MM-dd'T'HH_mm_ss"));
    final String outputFileName = inputFileName + "-" + completionTime + ".json";
    return "completed/result="
        + stepFunctionOutput.getStepFunctionOutcome().name()
        + "/input-file-name="
        + inputFileName
        + "."
        + inputFileExtension
        + "/completion-time="
        + completionTime
        + "/"
        + outputFileName;
  }
}
