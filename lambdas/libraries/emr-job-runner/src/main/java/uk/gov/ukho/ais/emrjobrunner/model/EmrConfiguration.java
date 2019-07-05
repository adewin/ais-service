package uk.gov.ukho.ais.emrjobrunner.model;

public class EmrConfiguration {
  private final String masterInstanceType;
  private final String workerInstanceType;
  private final String logUri;
  private final String serviceRole;
  private final String jobFlowRole;
  private final String clusterName;
  private final String emrVersion;
  private final String instanceCount;
  private final String driverMemory;
  private final String executorMemory;

  public EmrConfiguration(
      final String masterInstanceType,
      final String workerInstanceType,
      final String logUri,
      final String serviceRole,
      final String jobFlowRole,
      final String clusterName,
      final String emrVersion,
      final String instanceCount,
      final String driverMemory,
      final String executorMemory) {
    this.masterInstanceType = masterInstanceType;
    this.workerInstanceType = workerInstanceType;
    this.logUri = logUri;
    this.serviceRole = serviceRole;
    this.jobFlowRole = jobFlowRole;
    this.clusterName = clusterName;
    this.emrVersion = emrVersion;
    this.instanceCount = instanceCount;
    this.driverMemory = driverMemory;
    this.executorMemory = executorMemory;
  }

  public String getMasterInstanceType() {
    return masterInstanceType;
  }

  public String getWorkerInstanceType() {
    return workerInstanceType;
  }

  public String getLogUri() {
    return logUri;
  }

  public String getServiceRole() {
    return serviceRole;
  }

  public String getJobFlowRole() {
    return jobFlowRole;
  }

  public String getClusterName() {
    return clusterName;
  }

  public String getEmrVersion() {
    return emrVersion;
  }

  public String getInstanceCount() {
    return instanceCount;
  }

  public String getDriverMemory() {
    return driverMemory;
  }

  public String getExecutorMemory() {
    return executorMemory;
  }
}
