package uk.gov.ukho.ais.emrjobrunner;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.elasticmapreduce.AmazonElasticMapReduce;
import com.amazonaws.services.elasticmapreduce.AmazonElasticMapReduceClientBuilder;
import com.amazonaws.services.elasticmapreduce.model.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import uk.gov.ukho.ais.emrjobrunner.model.AbstractJob;
import uk.gov.ukho.ais.emrjobrunner.model.EmrConfiguration;

public class EmrJobRunner {

  private final EmrConfiguration emrConfiguration;
  private final StepConfigFactory stepConfigFactory;

  public EmrJobRunner(EmrConfiguration emrConfiguration) {
    this.emrConfiguration = emrConfiguration;
    this.stepConfigFactory = new StepConfigFactory();
  }

  public String runEmrJobs(final List<AbstractJob> jobs) {
    final AmazonElasticMapReduce emr = buildMapReduceClient();
    final List<Application> apps = buildApplications();
    final List<StepConfig> buildSteps = buildSteps(jobs);
    final RunJobFlowRequest request = buildJobFlowRequest(buildSteps, apps);

    return emr.runJobFlow(request).toString();
  }

  private AmazonElasticMapReduce buildMapReduceClient() {
    return AmazonElasticMapReduceClientBuilder.standard().withRegion(Regions.EU_WEST_2).build();
  }

  private List<Application> buildApplications() {

    return Collections.singletonList(new Application().withName("spark"));
  }

  private List<StepConfig> buildSteps(final List<AbstractJob> jobs) {

    return jobs.stream()
        .filter(AbstractJob::isActive)
        .map(stepConfigFactory::buildStepConfig)
        .collect(Collectors.toList());
  }

  private List<Configuration> buildSparkConfigurations() {

    return Arrays.asList(
        new Configuration()
            .withClassification("spark")
            .addPropertiesEntry("maximizeResourceAllocation", "true"),
        new Configuration()
            .withClassification("spark-defaults")
            .addPropertiesEntry("spark.driver.maxResultSize", "4g")
            .addPropertiesEntry("spark.kryoserializer.buffer.max", "1024m")
    );
  }

  private RunJobFlowRequest buildJobFlowRequest(
      final List<StepConfig> steps, final List<Application> apps) {

    return new RunJobFlowRequest()
        .withName(emrConfiguration.getClusterName())
        .withReleaseLabel(emrConfiguration.getEmrVersion())
        .withSteps(steps)
        .withApplications(apps)
        .withConfigurations(buildSparkConfigurations())
        .withLogUri(emrConfiguration.getLogUri())
        .withServiceRole(emrConfiguration.getServiceRole())
        .withJobFlowRole(emrConfiguration.getJobFlowRole())
        .withVisibleToAllUsers(true)
        .withInstances(
            new JobFlowInstancesConfig()
                .withInstanceCount(Integer.parseInt(emrConfiguration.getInstanceCount()))
                .withMasterInstanceType(emrConfiguration.getMasterInstanceType())
                .withSlaveInstanceType(emrConfiguration.getWorkerInstanceType())
                .withKeepJobFlowAliveWhenNoSteps(false));
  }
}
