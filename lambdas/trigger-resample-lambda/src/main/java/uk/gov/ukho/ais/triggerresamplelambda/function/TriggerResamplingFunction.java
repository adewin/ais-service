package uk.gov.ukho.ais.triggerresamplelambda.function;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import uk.gov.ukho.ais.emrjobrunner.EmrJobRunner;
import uk.gov.ukho.ais.emrjobrunner.model.AbstractJob;

public class TriggerResamplingFunction implements Function<String, String> {

  private final EmrJobRunner emrJobRunner;
  private final List<AbstractJob> emrJobs;

  public TriggerResamplingFunction(EmrJobRunner emrJobRunner, List<AbstractJob> emrJobs) {
    this.emrJobRunner = emrJobRunner;
    this.emrJobs = emrJobs;
  }

  @Override
  public String apply(String o) {
    return emrJobRunner.runEmrJobs(new ArrayList<>(this.emrJobs));
  }
}
