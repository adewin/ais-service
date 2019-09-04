package uk.gov.ukho.ais.triggerrawpartitioning.model;

import java.util.Arrays;
import java.util.List;
import uk.gov.ukho.ais.emrjobrunner.model.AbstractJob;
import uk.gov.ukho.ais.triggerrawpartitioning.configuration.EmrLambdaConfiguration;

public class PartitionJob extends AbstractJob {

  private final String fileReference;

  public PartitionJob(String fileReference) {
    super();
    this.fileReference = fileReference;
  }

  @Override
  public List<String> getJobSpecificParameters() {
    return Arrays.asList(
        "--class",
        EmrLambdaConfiguration.getInstance().getJobFullyQualifiedClassName(),
        EmrLambdaConfiguration.getInstance().getJobLocation(),
        "-i",
        fileReference,
        "-o",
        EmrLambdaConfiguration.getInstance().getOutputLocation(),
        "-d",
        EmrLambdaConfiguration.getInstance().getOutputDatabase(),
        "-t",
        EmrLambdaConfiguration.getInstance().getOutputTable());
  }
}
