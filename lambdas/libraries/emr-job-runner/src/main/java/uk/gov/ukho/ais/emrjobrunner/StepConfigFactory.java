package uk.gov.ukho.ais.emrjobrunner;

import com.amazonaws.services.elasticmapreduce.model.HadoopJarStepConfig;
import com.amazonaws.services.elasticmapreduce.model.StepConfig;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import uk.gov.ukho.ais.emrjobrunner.model.AbstractJob;
import uk.gov.ukho.ais.emrjobrunner.model.EmrConfiguration;

class StepConfigFactory {

  private final EmrConfiguration emrConfiguration;

  StepConfigFactory(EmrConfiguration emrConfiguration) {
    this.emrConfiguration = emrConfiguration;
  }

  StepConfig buildStepConfig(final AbstractJob job) {
    return createStepConfig(createSparkStepConfig(job));
  }

  private StepConfig createStepConfig(final HadoopJarStepConfig hadoopJarStepConfig) {
    return new StepConfig()
        .withName("Spark Step")
        .withActionOnFailure("CONTINUE")
        .withHadoopJarStep(hadoopJarStepConfig);
  }

  private HadoopJarStepConfig createSparkStepConfig(final AbstractJob job) {
    final List<String> args =
        new ArrayList<>(
            Arrays.asList(
                "spark-submit",
                "--conf",
                "spark.driver.maxResultSize=4g",
                "--driver-memory",
                emrConfiguration.getDriverMemory(),
                "--executor-memory",
                emrConfiguration.getExecutorMemory()));

    if (job.getJobSpecificParameters().size() > 0) {
      args.addAll(job.getJobSpecificParameters());
    }

    return new HadoopJarStepConfig().withJar("command-runner.jar").withArgs(args);
  }
}
