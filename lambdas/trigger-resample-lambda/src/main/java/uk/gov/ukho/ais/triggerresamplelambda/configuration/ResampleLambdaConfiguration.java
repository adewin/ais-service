package uk.gov.ukho.ais.triggerresamplelambda.configuration;

import uk.gov.ukho.ais.emrjobrunner.model.EmrConfiguration;

public class ResampleLambdaConfiguration extends EmrConfiguration {

  private final String queueUrl;
  private final String jobFullyQualifiedClassName;
  private final String jobLocation;
  private final String inputLocation;
  private final String outputLocation;

  public ResampleLambdaConfiguration() {
    super(
        System.getenv("INSTANCE_TYPE_MASTER"),
        System.getenv("INSTANCE_TYPE_WORKER"),
        System.getenv("LOG_URI"),
        System.getenv("SERVICE_ROLE"),
        System.getenv("JOB_FLOW_ROLE"),
        System.getenv("CLUSTER_NAME"),
        System.getenv("EMR_VERSION"),
        System.getenv("INSTANCE_COUNT"));

    queueUrl = System.getenv("QUEUE_URL");
    jobFullyQualifiedClassName = System.getenv("JOB_FULLY_QUALIFIED_CLASS_NAME");
    jobLocation = System.getenv("JOB_LOCATION");
    inputLocation = System.getenv("INPUT_LOCATION");
    outputLocation = System.getenv("OUTPUT_LOCATION");
  }

  public String getQueueUrl() {
    return queueUrl;
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
