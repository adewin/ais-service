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
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class AisBatchLambdaHandler implements RequestHandler<Integer, String> {

  private static final String JOB_FULLY_QUALIFIED_CLASS_NAME =
      System.getenv("JOB_FULLY_QUALIFIED_CLASS_NAME");
  private static final String JOB_LOCATION = System.getenv("JOB_LOCATION");
  private static final String INPUT_LOCATION = System.getenv("INPUT_LOCATION");
  private static final String OUTPUT_LOCATION = System.getenv("OUTPUT_LOCATION");
  private static final String INSTANCE_TYPE_MASTER = System.getenv("INSTANCE_TYPE_MASTER");
  private static final String INSTANCE_TYPE_WORKER = System.getenv("INSTANCE_TYPE_WORKER");
  private static final String LOG_URI = System.getenv("LOG_URI");
  private static final String SERVICE_ROLE = System.getenv("SERVICE_ROLE");
  private static final String JOB_FLOW_ROLE = System.getenv("JOB_FLOW_ROLE");
  private static final String CLUSTER_NAME = System.getenv("CLUSTER_NAME");
  private static final String EMR_VERSION = System.getenv("EMR_VERSION");
  private static final String INSTANCE_COUNT = System.getenv("INSTANCE_COUNT");
  private static final String DRIVER_MEMORY = System.getenv("DRIVER_MEMORY");
  private static final String EXECUTOR_MEMORY = System.getenv("EXECUTOR_MEMORY");

  @Override
  public String handleRequest(final Integer inputInt, final Context context) {

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
    return new HadoopJarStepConfig()
        .withJar("command-runner.jar")
        .withArgs(
            "spark-submit",
            "--conf",
            "spark.driver.maxResultSize=4g",
            "--driver-memory",
            DRIVER_MEMORY,
            "--executor-memory",
            EXECUTOR_MEMORY,
            "--class",
            JOB_FULLY_QUALIFIED_CLASS_NAME,
            JOB_LOCATION,
            "-i",
            INPUT_LOCATION,
            "-o",
            OUTPUT_LOCATION,
            "-p",
            job.getPrefix(),
            "-r",
            job.getResolution().toString(),
            "-d",
            String.valueOf(job.getDistanceInterpolationThreshold()),
            "-t",
            String.valueOf(job.getTimeInterpolationThreshold()),
            "-s",
            job.getStartPeriod(),
            "-e",
            job.getEndPeriod());
  }

  private RunJobFlowRequest buildJobFlowRequest(
      final List<StepConfig> steps, final List<Application> apps) {

    return new RunJobFlowRequest()
        .withName(CLUSTER_NAME)
        .withReleaseLabel(EMR_VERSION)
        .withSteps(steps)
        .withApplications(apps)
        .withLogUri(LOG_URI)
        .withServiceRole(SERVICE_ROLE)
        .withJobFlowRole(JOB_FLOW_ROLE)
        .withVisibleToAllUsers(true)
        .withInstances(
            new JobFlowInstancesConfig()
                .withInstanceCount(Integer.parseInt(INSTANCE_COUNT))
                .withMasterInstanceType(INSTANCE_TYPE_MASTER)
                .withSlaveInstanceType(INSTANCE_TYPE_WORKER)
                .withKeepJobFlowAliveWhenNoSteps(false));
  }
}
