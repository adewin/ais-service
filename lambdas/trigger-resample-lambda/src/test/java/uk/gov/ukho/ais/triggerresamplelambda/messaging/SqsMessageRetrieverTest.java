package uk.gov.ukho.ais.triggerresamplelambda.messaging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.PurgeQueueRequest;
import com.amazonaws.services.sqs.model.PurgeQueueResult;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.ukho.ais.triggerresamplelambda.configuration.ResampleLambdaConfiguration;

@RunWith(MockitoJUnitRunner.class)
public class SqsMessageRetrieverTest {

  private final String queueUrl = "https://queue.com";

  @Mock private AmazonSQS mockSqs;
  @Mock private ResampleLambdaConfiguration mockConfiguration;

  @InjectMocks private SqsMessageRetriever sqsMessageRetriever;

  @Rule public EnvironmentVariables environmentVariables = new EnvironmentVariables();

  @Captor private ArgumentCaptor<PurgeQueueRequest> purgeQueueRequestArgumentCaptor;

  @Before
  public void beforeEach() {
    environmentVariables.set("QUEUE_URL", queueUrl);
  }

  @Test
  public void whenSqsQueueHasNewMessageThenEmptiesQueueAndReturnsTrue() {
    final Message testMessage = new Message().withBody("{}");

    when(mockConfiguration.getQueueUrl()).thenReturn(queueUrl);
    when(mockSqs.receiveMessage(queueUrl))
        .thenReturn(new ReceiveMessageResult().withMessages(testMessage));
    when(mockSqs.purgeQueue(any())).thenReturn(new PurgeQueueResult());

    System.out.println("Message retriever:");
    System.out.println(sqsMessageRetriever);

    final boolean result = sqsMessageRetriever.hasMessages();

    assertThat(result).isTrue();

    verify(mockSqs, times(1)).purgeQueue(purgeQueueRequestArgumentCaptor.capture());

    assertThat(purgeQueueRequestArgumentCaptor.getValue().getQueueUrl()).isEqualTo(queueUrl);
  }

  @Test
  public void whenSqsQueueHasMultipleNewMessagesThenEmptiesQueueAndReturnsTrue() {

    final List<Message> messages =
        IntStream.rangeClosed(1, 9)
            .boxed()
            .map(i -> new Message().withBody("{}"))
            .collect(Collectors.toList());

    when(mockConfiguration.getQueueUrl()).thenReturn(queueUrl);
    when(mockConfiguration.getQueueUrl()).thenReturn(queueUrl);
    when(mockSqs.receiveMessage(queueUrl))
        .thenReturn(new ReceiveMessageResult().withMessages(messages));

    final boolean result = sqsMessageRetriever.hasMessages();

    assertThat(result).isTrue();

    verify(mockSqs, times(1)).purgeQueue(purgeQueueRequestArgumentCaptor.capture());

    assertThat(purgeQueueRequestArgumentCaptor.getValue().getQueueUrl()).isEqualTo(queueUrl);
  }

  @Test
  public void whenSqsQueueIsEmptyThenReturnsFalse() {
    when(mockConfiguration.getQueueUrl()).thenReturn(queueUrl);
    when(mockSqs.receiveMessage(queueUrl))
        .thenReturn(new ReceiveMessageResult().withMessages(Collections.emptyList()));

    final boolean result = sqsMessageRetriever.hasMessages();

    assertThat(result).isFalse();

    verify(mockSqs, never()).purgeQueue(any());
  }
}
