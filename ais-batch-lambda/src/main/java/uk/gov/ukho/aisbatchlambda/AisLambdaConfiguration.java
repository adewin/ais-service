package uk.gov.ukho.aisbatchlambda;

public final class AisLambdaConfiguration {

  public static final String JOB_FULLY_QUALIFIED_CLASS_NAME =
      System.getenv("JOB_FULLY_QUALIFIED_CLASS_NAME");
  public static final String JOB_LOCATION = System.getenv("JOB_LOCATION");
  public static final String INPUT_LOCATION = System.getenv("INPUT_LOCATION");
  public static final String DEFAULT_OUTPUT_LOCATION = System.getenv("OUTPUT_LOCATION");
  public static final String SENSITIVE_OUTPUT_LOCATION = System.getenv("SENSITIVE_OUTPUT_LOCATION");
  public static final String INSTANCE_TYPE_MASTER = System.getenv("INSTANCE_TYPE_MASTER");
  public static final String INSTANCE_TYPE_WORKER = System.getenv("INSTANCE_TYPE_WORKER");
  public static final String LOG_URI = System.getenv("LOG_URI");
  public static final String SERVICE_ROLE = System.getenv("SERVICE_ROLE");
  public static final String JOB_FLOW_ROLE = System.getenv("JOB_FLOW_ROLE");
  public static final String CLUSTER_NAME = System.getenv("CLUSTER_NAME");
  public static final String EMR_VERSION = System.getenv("EMR_VERSION");
  public static final String INSTANCE_COUNT = System.getenv("INSTANCE_COUNT");
  public static final String DRIVER_MEMORY = System.getenv("DRIVER_MEMORY");
  public static final String EXECUTOR_MEMORY = System.getenv("EXECUTOR_MEMORY");
  public static final String DRAUGHT_CONFIG_FILE = System.getenv("DRAUGHT_CONFIG_FILE");
  public static final String STATIC_DATA_FILE = System.getenv("DRAUGHT_CONFIG_FILE");

  private AisLambdaConfiguration() {}
}
