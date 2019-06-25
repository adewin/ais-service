package uk.gov.ukho.aisbatchlambda;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.elasticmapreduce.AmazonElasticMapReduce;
import com.amazonaws.services.elasticmapreduce.AmazonElasticMapReduceClientBuilder;
import com.amazonaws.services.elasticmapreduce.model.Application;
import com.amazonaws.services.elasticmapreduce.model.HadoopJarStepConfig;
import com.amazonaws.services.elasticmapreduce.model.JobFlowInstancesConfig;
import com.amazonaws.services.elasticmapreduce.model.RunJobFlowRequest;
import com.amazonaws.services.elasticmapreduce.model.StepConfig;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class AisBatchLambdaHandler implements RequestHandler<Integer, String> {

  @Override
  public String handleRequest(final Integer _unusedInput, final Context context) {

    final AmazonElasticMapReduce emr = buildMapReduceClient();
    final List<Application> apps = buildApplications();
    final List<StepConfig> buildSteps = buildSteps();
    final RunJobFlowRequest request = buildJobFlowRequest(buildSteps, apps);

    return emr.runJobFlow(request).toString();
  }

  private AmazonElasticMapReduce buildMapReduceClient() {

    return AmazonElasticMapReduceClientBuilder.standard().withRegion(Regions.EU_WEST_2).build();
  }

  private List<Application> buildApplications() {

    return Collections.singletonList(new Application().withName("spark"));
  }

  private List<StepConfig> buildSteps() {

    return JobsProvider.getInstance().getJobs().stream()
        .filter(Job::isActive)
        .map(this::createSparkStepConfig)
        .map(this::createStepConfig)
        .collect(Collectors.toList());
  }

  private StepConfig createStepConfig(final HadoopJarStepConfig hadoopJarStepConfig) {
    return new StepConfig()
        .withName("Spark Step")
        .withActionOnFailure("CONTINUE")
        .withHadoopJarStep(hadoopJarStepConfig);
  }

  private HadoopJarStepConfig createSparkStepConfig(final Job job) {
    final List<String> args =
        new ArrayList<>(
            Arrays.asList(
                "spark-submit",
                "--conf",
                "spark.driver.maxResultSize=4g",
                "--driver-memory",
                AisLambdaConfiguration.DRIVER_MEMORY,
                "--executor-memory",
                AisLambdaConfiguration.EXECUTOR_MEMORY,
                "--class",
                AisLambdaConfiguration.JOB_FULLY_QUALIFIED_CLASS_NAME,
                AisLambdaConfiguration.JOB_LOCATION,
                "-i",
                AisLambdaConfiguration.INPUT_LOCATION,
                "-o",
                job.getIsOutputLocationSensitive()
                    ? AisLambdaConfiguration.SENSITIVE_OUTPUT_LOCATION
                    : AisLambdaConfiguration.DEFAULT_OUTPUT_LOCATION,
                "-p",
                job.getPrefix(),
                "-r",
                String.valueOf(job.getResolution()),
                "-d",
                String.valueOf(job.getDistanceInterpolationThreshold()),
                "-t",
                String.valueOf(job.getTimeInterpolationThreshold()),
                "-s",
                job.getStartPeriod(),
                "-e",
                job.getEndPeriod(),
                "--draughtConfigFile",
                AisLambdaConfiguration.DRAUGHT_CONFIG_FILE,
                "--staticDataFile",
                AisLambdaConfiguration.STATIC_DATA_FILE));

    if (job.getDraughtIndex() != null) {
      args.addAll(Arrays.asList("--draughtIndex", job.getDraughtIndex()));
    }

    return new HadoopJarStepConfig().withJar("command-runner.jar").withArgs(args);
  }

  private RunJobFlowRequest buildJobFlowRequest(
      final List<StepConfig> steps, final List<Application> apps) {

    return new RunJobFlowRequest()
        .withName(AisLambdaConfiguration.CLUSTER_NAME)
        .withReleaseLabel(AisLambdaConfiguration.EMR_VERSION)
        .withSteps(steps)
        .withApplications(apps)
        .withLogUri(AisLambdaConfiguration.LOG_URI)
        .withServiceRole(AisLambdaConfiguration.SERVICE_ROLE)
        .withJobFlowRole(AisLambdaConfiguration.JOB_FLOW_ROLE)
        .withVisibleToAllUsers(true)
        .withInstances(
            new JobFlowInstancesConfig()
                .withInstanceCount(Integer.parseInt(AisLambdaConfiguration.INSTANCE_COUNT))
                .withMasterInstanceType(AisLambdaConfiguration.INSTANCE_TYPE_MASTER)
                .withSlaveInstanceType(AisLambdaConfiguration.INSTANCE_TYPE_WORKER)
                .withKeepJobFlowAliveWhenNoSteps(false));
  }
}
