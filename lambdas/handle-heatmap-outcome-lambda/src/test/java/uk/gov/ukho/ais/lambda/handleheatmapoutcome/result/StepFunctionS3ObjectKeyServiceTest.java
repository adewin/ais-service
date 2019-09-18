package uk.gov.ukho.ais.lambda.handleheatmapoutcome.result;

import cyclops.control.Option;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import org.assertj.core.api.SoftAssertions;
import org.junit.Test;
import uk.gov.ukho.ais.lambda.heatmap.job.model.JobConfig;
import uk.gov.ukho.ais.lambda.heatmap.job.model.StepFunctionOutcome;
import uk.gov.ukho.ais.lambda.heatmap.job.model.StepFunctionOutput;

public class StepFunctionS3ObjectKeyServiceTest {

  @Test
  public void whenStepFunctionOutputSuppliedThenS3KeyGenerated() {
    SoftAssertions.assertSoftly(
        softly -> {
          final Clock testClock =
              Clock.fixed(Instant.parse("2019-04-11T00:00:00Z"), ZoneId.of("UTC"));

          final StepFunctionS3ObjectKeyService stepFunctionS3ObjectKeyService =
              new StepFunctionS3ObjectKeyService(testClock);

          final String inputFileBaseName = "test-conf";
          final StepFunctionOutput stepFunctionOutput =
              new StepFunctionOutput(
                  "2dda",
                  StepFunctionOutcome.SUCCESS,
                  "s3://test-bucket/submit/" + inputFileBaseName + ".json",
                  JobConfig.empty(),
                  Option.none(),
                  Option.none());

          final String result =
              stepFunctionS3ObjectKeyService.createS3CompletedObjectKey(stepFunctionOutput);

          softly
              .assertThat(result)
              .isEqualTo(
                  "completed/result=SUCCESS/input-file-name="
                      + inputFileBaseName
                      + ".json/completion-time=2019-04-11T00_00_00/test-conf-2019-04-11T00_00_00.json");
        });
  }
}
