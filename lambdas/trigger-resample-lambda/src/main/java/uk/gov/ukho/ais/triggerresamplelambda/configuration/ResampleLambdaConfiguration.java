package uk.gov.ukho.ais.triggerresamplelambda.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.ukho.ais.emrjobrunner.model.EmrConfiguration;

@Component
public class ResampleLambdaConfiguration extends EmrConfiguration {

  private final String jobFullyQualifiedClassName;
  private final String jobLocation;
  private final String inputLocation;
  private final String outputLocation;

  @Autowired
  public ResampleLambdaConfiguration(
      @Value("${INSTANCE_TYPE_MASTER}") final String masterInstanceType,
      @Value("${INSTANCE_TYPE_WORKER}") final String workerInstanceType,
      @Value("${LOG_URI}") final String logUri,
      @Value("${SERVICE_ROLE}") final String serviceRole,
      @Value("${JOB_FLOW_ROLE}") final String jobFlowRole,
      @Value("${CLUSTER_NAME}") final String clusterName,
      @Value("${EMR_VERSION}") final String emrVersion,
      @Value("${INSTANCE_COUNT}") final String instanceCount,
      @Value("${DRIVER_MEMORY}") final String driverMemory,
      @Value("${EXECUTOR_MEMORY}") final String executorMemory,
      @Value("${JOB_FULLY_QUALIFIED_CLASS_NAME}") final String jobFullyQualifiedClassName,
      @Value("${JOB_LOCATION}") final String jobLocation,
      @Value("${INPUT_LOCATION}") final String inputLocation,
      @Value("${OUTPUT_LOCATION}") final String outputLocation) {
    super(
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
    this.jobFullyQualifiedClassName = jobFullyQualifiedClassName;
    this.jobLocation = jobLocation;
    this.inputLocation = inputLocation;
    this.outputLocation = outputLocation;
  }

  public String getJobFullyQualifiedClassName() {
    return jobFullyQualifiedClassName;
  }

  public String getJobLocation() {
    return jobLocation;
  }

  public String getInputLocation() {
    return inputLocation;
  }

  public String getOutputLocation() {
    return outputLocation;
  }
}
