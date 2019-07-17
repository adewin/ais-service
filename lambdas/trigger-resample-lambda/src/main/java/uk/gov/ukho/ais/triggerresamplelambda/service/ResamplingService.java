package uk.gov.ukho.ais.triggerresamplelambda.service;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import uk.gov.ukho.ais.emrjobrunner.EmrJobRunner;
import uk.gov.ukho.ais.emrjobrunner.model.AbstractJob;
import uk.gov.ukho.ais.triggerresamplelambda.messaging.SqsMessageRetriever;
import uk.gov.ukho.ais.triggerresamplelambda.s3.BucketEmptier;

public class ResamplingService {

  private SqsMessageRetriever sqsMessageRetriever;
  private EmrJobRunner emrJobRunner;
  private Supplier<List<AbstractJob>> resampleJobsSupplier;
  private BucketEmptier bucketEmptier;

  public ResamplingService(
      final SqsMessageRetriever sqsMessageRetriever,
      final EmrJobRunner emrJobRunner,
      final Supplier<List<AbstractJob>> resampleJobsSupplier,
      final BucketEmptier bucketEmptier) {
    this.sqsMessageRetriever = sqsMessageRetriever;
    this.emrJobRunner = emrJobRunner;
    this.resampleJobsSupplier = resampleJobsSupplier;
    this.bucketEmptier = bucketEmptier;
  }

  public String resampleData() {
    if (sqsMessageRetriever.hasMessages()) {
      return emptyBucketAndResampleData();
    }

    return "";
  }

  private String emptyBucketAndResampleData() {
    bucketEmptier.emptyResampledBucket();
    return emrJobRunner.runEmrJobs(new ArrayList<>(resampleJobsSupplier.get()));
  }
}
