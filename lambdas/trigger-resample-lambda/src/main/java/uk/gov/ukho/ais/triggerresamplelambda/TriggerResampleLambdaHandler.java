package uk.gov.ukho.ais.triggerresamplelambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import uk.gov.ukho.ais.triggerresamplelambda.service.ResamplingServiceSupplier;

public class TriggerResampleLambdaHandler implements RequestHandler<Object, String> {

  private final ResamplingServiceSupplier resamplingServiceSupplier;

  public TriggerResampleLambdaHandler() {
    resamplingServiceSupplier = new ResamplingServiceSupplier();
  }

  @Override
  public String handleRequest(Object notUsed, Context context) {
    return resamplingServiceSupplier.get().resampleData();
  }
}
