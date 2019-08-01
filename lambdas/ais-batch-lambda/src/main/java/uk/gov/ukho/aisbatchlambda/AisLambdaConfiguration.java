package uk.gov.ukho.aisbatchlambda;

import uk.gov.ukho.ais.emrjobrunner.EmrJobRunner;
import uk.gov.ukho.ais.emrjobrunner.model.EmrConfiguration;

public final class AisLambdaConfiguration {

  public static final String JOB_FULLY_QUALIFIED_CLASS_NAME =
      System.getenv("JOB_FULLY_QUALIFIED_CLASS_NAME");
  public static final String JOB_LOCATION = System.getenv("JOB_LOCATION");
  public static final String INPUT_LOCATION = System.getenv("INPUT_LOCATION");
  public static final String DEFAULT_OUTPUT_LOCATION = System.getenv("OUTPUT_LOCATION");
  public static final String SENSITIVE_OUTPUT_LOCATION = System.getenv("SENSITIVE_OUTPUT_LOCATION");
  public static final String DRAUGHT_CONFIG_FILE = System.getenv("DRAUGHT_CONFIG_FILE");
  public static final String STATIC_DATA_FILE = System.getenv("STATIC_DATA_FILE");
  private static final String INSTANCE_TYPE_MASTER = System.getenv("INSTANCE_TYPE_MASTER");
  private static final String INSTANCE_TYPE_WORKER = System.getenv("INSTANCE_TYPE_WORKER");
  private static final String LOG_URI = System.getenv("LOG_URI");
  private static final String SERVICE_ROLE = System.getenv("SERVICE_ROLE");
  private static final String JOB_FLOW_ROLE = System.getenv("JOB_FLOW_ROLE");
  private static final String CLUSTER_NAME = System.getenv("CLUSTER_NAME");
  private static final String EMR_VERSION = System.getenv("EMR_VERSION");
  private static final String INSTANCE_COUNT = System.getenv("INSTANCE_COUNT");

  private static EmrJobRunner emrJobRunner = null;

  private AisLambdaConfiguration() {}

  public static EmrJobRunner getEmrJobRunnerInstance() {
    if (emrJobRunner == null) {
      final EmrConfiguration emrConfiguration =
          new EmrConfiguration(
              INSTANCE_TYPE_MASTER,
              INSTANCE_TYPE_WORKER,
              LOG_URI,
              SERVICE_ROLE,
              JOB_FLOW_ROLE,
              CLUSTER_NAME,
              EMR_VERSION,
              INSTANCE_COUNT);
      emrJobRunner = new EmrJobRunner(emrConfiguration);
    }
    return emrJobRunner;
  }
}
