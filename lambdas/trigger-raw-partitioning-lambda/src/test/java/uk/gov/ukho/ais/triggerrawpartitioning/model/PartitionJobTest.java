package uk.gov.ukho.ais.triggerrawpartitioning.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import uk.gov.ukho.ais.emrjobrunner.model.AbstractJob;
import uk.gov.ukho.ais.s3eventhandling.model.S3Object;
import uk.gov.ukho.ais.s3eventhandling.model.S3ObjectEvent;

public class PartitionJobTest {

  private final String bucket = "my-bucket";
  private final String key = "data/file/one.txt";
  private final String instanceTypeMaster = "instanceTypeMaster";
  private final String instanceTypeWorker = "instanceTypeWorker";
  private final String logUri = "logUri";
  private final String serviceRole = "serviceRole";
  private final String jobFlowRole = "jobFlowRole";
  private final String clusterName = "clusterName";
  private final String emrVersion = "emrVersion";
  private final String instanceCount = "instanceCount";
  private final String className = "className";
  private final String jobLocation = "jobLocation";

  @Rule public EnvironmentVariables environmentVariables = new EnvironmentVariables();
  private final String outputLocation = "outputLocation";

  @Before
  public void beforeEach() {
    environmentVariables.set("INSTANCE_TYPE_MASTER", instanceTypeMaster);
    environmentVariables.set("INSTANCE_TYPE_WORKER", instanceTypeWorker);
    environmentVariables.set("LOG_URI", logUri);
    environmentVariables.set("SERVICE_ROLE", serviceRole);
    environmentVariables.set("JOB_FLOW_ROLE", jobFlowRole);
    environmentVariables.set("CLUSTER_NAME", clusterName);
    environmentVariables.set("EMR_VERSION", emrVersion);
    environmentVariables.set("INSTANCE_COUNT", instanceCount);
    environmentVariables.set("JOB_FULLY_QUALIFIED_CLASS_NAME", className);
    environmentVariables.set("JOB_LOCATION", jobLocation);
    environmentVariables.set("OUTPUT_LOCATION", outputLocation);
  }

  @Test
  public void whenGettingJobSpecificParametersThenReturnsListOfParameters() {

    final S3Object s3Object = new S3Object(bucket, key, S3ObjectEvent.CREATED);

    final AbstractJob result = new PartitionJob(s3Object.toString());

    assertThat(result.getJobSpecificParameters())
        .containsExactly(
            "--class",
            className,
            jobLocation,
            "-i",
            "s3a://" + bucket + "/" + key,
            "-o",
            outputLocation);
  }
}
