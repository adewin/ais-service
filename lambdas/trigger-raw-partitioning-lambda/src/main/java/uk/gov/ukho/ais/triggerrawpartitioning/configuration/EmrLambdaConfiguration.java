package uk.gov.ukho.ais.triggerrawpartitioning.configuration;

import uk.gov.ukho.ais.emrjobrunner.model.EmrConfiguration;

public class EmrLambdaConfiguration extends EmrConfiguration {

  private static EmrLambdaConfiguration instance = null;

  private final String jobFullyQualifiedClassName;
  private final String jobLocation;
  private final String outputLocation;
  private final String outputDatabase;
  private final String outputTable;

  private EmrLambdaConfiguration() {
    super(
        System.getenv("INSTANCE_TYPE_MASTER"),
        System.getenv("INSTANCE_TYPE_WORKER"),
        System.getenv("LOG_URI"),
        System.getenv("SERVICE_ROLE"),
        System.getenv("JOB_FLOW_ROLE"),
        System.getenv("CLUSTER_NAME"),
        System.getenv("EMR_VERSION"),
        System.getenv("INSTANCE_COUNT"),
        System.getenv("DRIVER_MEMORY"),
        System.getenv("EXECUTOR_MEMORY"));

    jobFullyQualifiedClassName = System.getenv("JOB_FULLY_QUALIFIED_CLASS_NAME");
    jobLocation = System.getenv("JOB_LOCATION");
    outputLocation = System.getenv("OUTPUT_LOCATION");
    outputDatabase = System.getenv("OUTPUT_DATABASE");
    outputTable = System.getenv("OUTPUT_TABLE");
  }

  public static EmrLambdaConfiguration getInstance() {
    if (instance == null) {
      instance = new EmrLambdaConfiguration();
    }

    return instance;
  }

  public String getJobFullyQualifiedClassName() {
    return jobFullyQualifiedClassName;
  }

  public String getJobLocation() {
    return jobLocation;
  }

  public String getOutputLocation() {
    return outputLocation;
  }

  public String getOutputDatabase() {
    return outputDatabase;
  }

  public String getOutputTable() {
    return outputTable;
  }
}
