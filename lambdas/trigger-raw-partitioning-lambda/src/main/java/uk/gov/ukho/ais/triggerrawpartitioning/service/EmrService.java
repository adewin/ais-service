package uk.gov.ukho.ais.triggerrawpartitioning.service;

import com.amazonaws.services.lambda.runtime.events.S3Event;
import java.util.List;
import java.util.stream.Collectors;
import uk.gov.ukho.ais.emrjobrunner.EmrJobRunner;
import uk.gov.ukho.ais.emrjobrunner.model.AbstractJob;
import uk.gov.ukho.ais.s3eventhandling.model.S3Object;
import uk.gov.ukho.ais.s3eventhandling.model.S3ObjectEvent;
import uk.gov.ukho.ais.s3eventhandling.service.S3NotificationFilter;
import uk.gov.ukho.ais.s3eventhandling.service.S3ObjectExtractor;
import uk.gov.ukho.ais.triggerrawpartitioning.model.PartitionJob;

public class EmrService {

  private EmrJobRunner emrJobRunner;

  public EmrService(EmrJobRunner emrJobRunner) {
    this.emrJobRunner = emrJobRunner;
  }

  public String triggerPartitionJobsForS3Event(final S3Event event) {
    List<AbstractJob> jobsToRun =
        S3ObjectExtractor.extractS3Objects(event).stream()
            .filter(S3NotificationFilter.filterObjectsForEventType(S3ObjectEvent.CREATED))
            .map(S3Object::toString)
            .map(PartitionJob::new)
            .collect(Collectors.toList());

    return jobsToRun.size() > 0 ? emrJobRunner.runEmrJobs(jobsToRun) : null;
  }
}
