package uk.gov.ukho.ais.triggerresamplelambda.service;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.sqs.AmazonSQSClient;
import java.util.List;
import java.util.function.Supplier;
import uk.gov.ukho.ais.emrjobrunner.EmrJobRunner;
import uk.gov.ukho.ais.emrjobrunner.model.AbstractJob;
import uk.gov.ukho.ais.triggerresamplelambda.configuration.ResampleLambdaConfiguration;
import uk.gov.ukho.ais.triggerresamplelambda.messaging.SqsMessageRetriever;
import uk.gov.ukho.ais.triggerresamplelambda.model.ResampleJobsSupplier;
import uk.gov.ukho.ais.triggerresamplelambda.s3.BucketEmptier;

public class ResamplingServiceSupplier implements Supplier<ResamplingService> {

  private static final ResampleLambdaConfiguration RESAMPLE_LAMBDA_CONFIGURATION =
      new ResampleLambdaConfiguration();

  private static SqsMessageRetriever sqsMessageRetrieverInstance() {
    return new SqsMessageRetriever(
        AmazonSQSClient.builder().build(), RESAMPLE_LAMBDA_CONFIGURATION);
  }

  private static EmrJobRunner emrJobRunnerInstance() {
    return new EmrJobRunner(RESAMPLE_LAMBDA_CONFIGURATION);
  }

  private static Supplier<List<AbstractJob>> resampleJobsSupplier() {
    return new ResampleJobsSupplier(new ResampleLambdaConfiguration());
  }

  private static BucketEmptier bucketEmptier() {
    return new BucketEmptier(AmazonS3Client.builder().build(), RESAMPLE_LAMBDA_CONFIGURATION);
  }

  @Override
  public ResamplingService get() {
    return new ResamplingService(
        sqsMessageRetrieverInstance(),
        emrJobRunnerInstance(),
        resampleJobsSupplier(),
        bucketEmptier());
  }
}
