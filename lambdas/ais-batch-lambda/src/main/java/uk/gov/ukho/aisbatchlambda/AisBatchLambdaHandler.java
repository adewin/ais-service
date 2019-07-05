package uk.gov.ukho.aisbatchlambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

public class AisBatchLambdaHandler implements RequestHandler<Integer, String> {

  @Override
  public String handleRequest(final Integer _unusedInput, final Context context) {
    return AisLambdaConfiguration.getEmrJobRunnerInstance()
        .runEmrJobs(JobsProvider.getInstance().getJobs());
  }
}
