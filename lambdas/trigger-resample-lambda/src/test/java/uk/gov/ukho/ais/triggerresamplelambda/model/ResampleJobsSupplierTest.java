package uk.gov.ukho.ais.triggerresamplelambda.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import uk.gov.ukho.ais.emrjobrunner.model.AbstractJob;
import uk.gov.ukho.ais.triggerresamplelambda.configuration.ResampleLambdaConfiguration;

public class ResampleJobsSupplierTest {

  private final String jobLocation = "jobLocation";
  private final String inputLocation = "inputLocation";
  private final String outputLocation = "outputLocation";
  private final String jobClass = "jobClass";

  @Rule public EnvironmentVariables environmentVariables = new EnvironmentVariables();

  @Before
  public void beforeEach() {
    environmentVariables.set("JOB_FULLY_QUALIFIED_CLASS_NAME", jobClass);
    environmentVariables.set("JOB_LOCATION", jobLocation);
    environmentVariables.set("INPUT_LOCATION", inputLocation);
    environmentVariables.set("OUTPUT_LOCATION", outputLocation);
  }

  @Test
  public void whenGetCalledThenReturnsTwoResampleJobs() {

    final String thirtyKm = "30000";
    final String sixHours = "21600000";
    final String oneHundredKm = "100000";
    final String eighteenHours = "64800000";

    final ResampleJobsSupplier resampleJobsSupplier =
        new ResampleJobsSupplier(new ResampleLambdaConfiguration());

    final List<AbstractJob> result = resampleJobsSupplier.get();

    assertThat(result)
        .extracting(job -> job.getJobSpecificParameters())
        .containsExactlyInAnyOrder(
            Arrays.asList(
                "--class",
                jobClass,
                jobLocation,
                "-i",
                inputLocation,
                "-o",
                outputLocation + "/30km_6hr/",
                "-d",
                thirtyKm,
                "-t",
                sixHours),
            Arrays.asList(
                "--class",
                jobClass,
                jobLocation,
                "-i",
                inputLocation,
                "-o",
                outputLocation + "/100km_18hr/",
                "-d",
                oneHundredKm,
                "-t",
                eighteenHours));
  }
}
