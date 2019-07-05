package uk.gov.ukho.ais.triggerrawpartitioning.service;

import java.util.function.Supplier;
import uk.gov.ukho.ais.emrjobrunner.EmrJobRunner;
import uk.gov.ukho.ais.triggerrawpartitioning.configuration.EmrLambdaConfiguration;

public class EmrServiceSupplier implements Supplier<EmrService> {

  private static EmrJobRunner emrJobRunnerInstance = null;

  @Override
  public EmrService get() {
    return new EmrService(getEmrJobRunnerInstance());
  }

  private static EmrJobRunner getEmrJobRunnerInstance() {
    if (emrJobRunnerInstance == null) {
      emrJobRunnerInstance = new EmrJobRunner(EmrLambdaConfiguration.getInstance());
    }
    return emrJobRunnerInstance;
  }
}
