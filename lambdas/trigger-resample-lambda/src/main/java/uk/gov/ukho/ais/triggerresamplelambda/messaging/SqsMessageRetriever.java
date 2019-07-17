package uk.gov.ukho.ais.triggerresamplelambda.messaging;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.PurgeQueueRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import uk.gov.ukho.ais.triggerresamplelambda.configuration.ResampleLambdaConfiguration;

public class SqsMessageRetriever {
  private AmazonSQS amazonSQS;
  private ResampleLambdaConfiguration resampleLambdaConfiguration;

  public SqsMessageRetriever(
      final AmazonSQS amazonSQS, final ResampleLambdaConfiguration resampleLambdaConfiguration) {
    this.amazonSQS = amazonSQS;
    this.resampleLambdaConfiguration = resampleLambdaConfiguration;
  }

  public boolean hasMessages() {
    final String queueUrl = resampleLambdaConfiguration.getQueueUrl();

    final ReceiveMessageResult receiveMessageResult = amazonSQS.receiveMessage(queueUrl);

    if (!receiveMessageResult.getMessages().isEmpty()) {
      amazonSQS.purgeQueue(new PurgeQueueRequest(queueUrl));

      return true;
    }

    return false;
  }
}
