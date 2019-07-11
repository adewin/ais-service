package uk.gov.ukho.ais.emrjobrunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.amazonaws.services.elasticmapreduce.model.HadoopJarStepConfig;
import com.amazonaws.services.elasticmapreduce.model.StepConfig;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.ukho.ais.emrjobrunner.model.AbstractJob;
import uk.gov.ukho.ais.emrjobrunner.model.EmrConfiguration;

@RunWith(MockitoJUnitRunner.class)
public class StepConfigFactoryTest {

  @Mock private AbstractJob mockJob;

  private final String masterInstanceType = "masterInstanceType";
  private final String workerInstanceType = "workerInstanceType";
  private final String logUri = "logUri";
  private final String serviceRole = "serviceRole";
  private final String jobFlowRole = "jobFlowRole";
  private final String clusterName = "clusterName";
  private final String emrVersion = "emrVersion";
  private final String instanceCount = "instanceCount";
  private final String driverMemory = "driverMemory";
  private final String executorMemory = "executorMemory";
  private final EmrConfiguration emrConfiguration =
      new EmrConfiguration(
          masterInstanceType,
          workerInstanceType,
          logUri,
          serviceRole,
          jobFlowRole,
          clusterName,
          emrVersion,
          instanceCount,
          driverMemory,
          executorMemory);
  private final StepConfigFactory stepConfigFactory = new StepConfigFactory(emrConfiguration);

  @Test
  public void whenGivenJobThenCreatesStepConfig() {
    final List<String> jobSpecificParameters = Arrays.asList("-i", "input", "-o", "output");
    when(mockJob.getJobSpecificParameters()).thenReturn(jobSpecificParameters);

    final StepConfig result = stepConfigFactory.buildStepConfig(mockJob);

    assertThat(result)
        .extracting("name", "actionOnFailure")
        .containsExactly("Spark Step", "CONTINUE");

    final HadoopJarStepConfig hadoopJarStepConfig = result.getHadoopJarStep();

    assertThat(hadoopJarStepConfig.getJar()).isEqualTo("command-runner.jar");

    assertThat(hadoopJarStepConfig.getArgs())
        .containsExactly(
            "spark-submit",
            "--conf",
            "spark.driver.maxResultSize=4g",
            "--conf",
            "spark.kryoserializer.buffer.max=1024m",
            "--driver-memory",
            driverMemory,
            "--executor-memory",
            executorMemory,
            "-i",
            "input",
            "-o",
            "output");
  }

  @Test
  public void whenGivenJobHasNoParametersThenCreatesStepConfigWithoutJobParameters() {
    when(mockJob.getJobSpecificParameters()).thenReturn(Collections.emptyList());

    final StepConfig result = stepConfigFactory.buildStepConfig(mockJob);

    assertThat(result)
        .extracting("name", "actionOnFailure")
        .containsExactly("Spark Step", "CONTINUE");

    final HadoopJarStepConfig hadoopJarStepConfig = result.getHadoopJarStep();

    assertThat(hadoopJarStepConfig.getJar()).isEqualTo("command-runner.jar");

    assertThat(hadoopJarStepConfig.getArgs())
        .containsExactly(
            "spark-submit",
            "--conf",
            "spark.driver.maxResultSize=4g",
            "--conf",
            "spark.kryoserializer.buffer.max=1024m",
            "--driver-memory",
            driverMemory,
            "--executor-memory",
            executorMemory);
  }
}
