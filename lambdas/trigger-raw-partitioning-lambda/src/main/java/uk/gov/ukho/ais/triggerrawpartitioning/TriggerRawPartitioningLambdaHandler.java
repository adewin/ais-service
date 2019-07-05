package uk.gov.ukho.ais.triggerrawpartitioning;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import uk.gov.ukho.ais.triggerrawpartitioning.service.EmrServiceSupplier;

public class TriggerRawPartitioningLambdaHandler implements RequestHandler<S3Event, String> {

  private final EmrServiceSupplier emrServiceSupplier;

  public TriggerRawPartitioningLambdaHandler() {
    emrServiceSupplier = new EmrServiceSupplier();
  }

  @Override
  public String handleRequest(final S3Event input, final Context context) {
    return emrServiceSupplier.get().triggerPartitionJobsForS3Event(input);
  }
}
