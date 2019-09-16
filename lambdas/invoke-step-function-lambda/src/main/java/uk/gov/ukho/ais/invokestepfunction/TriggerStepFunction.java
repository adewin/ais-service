package uk.gov.ukho.ais.invokestepfunction;

import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.s3.event.S3EventNotification;
import com.amazonaws.services.stepfunctions.AWSStepFunctions;
import com.amazonaws.services.stepfunctions.model.StartExecutionRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import cyclops.control.Option;
import cyclops.control.Try;
import java.util.UUID;
import java.util.function.Function;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class TriggerStepFunction implements Function<S3Event, InvokeStepFunctionResult> {

  private final String stepFunctionArn;
  private final ObjectMapper objectMapper;
  private final AWSStepFunctions stepFunctions;

  public TriggerStepFunction(
      @Value("${STEP_FUNCTION_ID}") final String stepFunctionArn,
      ObjectMapper objectMapper,
      final AWSStepFunctions stepFunctions) {
    this.stepFunctionArn = stepFunctionArn;
    this.objectMapper = objectMapper;
    this.stepFunctions = stepFunctions;
  }

  @Override
  public InvokeStepFunctionResult apply(S3Event s3Event) {
    final S3EventNotification.S3Entity s3Notification = s3Event.getRecords().get(0).getS3();
    return startExecutionRequestFor(s3Notification)
        .flatMap(
            startExecutionRequest ->
                Try.withCatch(() -> stepFunctions.startExecution(startExecutionRequest)))
        .map(
            startExecutionResult ->
                new InvokeStepFunctionResult(
                    stepFunctionArn, Option.of(startExecutionResult.getExecutionArn())))
        .orElse(new InvokeStepFunctionResult(stepFunctionArn, Option.none()));
  }

  private String getS3UriFromNotification(final S3EventNotification.S3Entity s3Notification) {
    final String bucketName = s3Notification.getBucket().getName();
    final String objectKey = s3Notification.getObject().getKey();
    return "s3://" + bucketName + "/" + objectKey;
  }

  private Try<StartExecutionRequest, JsonProcessingException> startExecutionRequestFor(
      final S3EventNotification.S3Entity s3Notification) {
    final String jobConfigUri = getS3UriFromNotification(s3Notification);

    return getStepFunctionInput(jobConfigUri)
        .map(
            stateMachineInput ->
                new StartExecutionRequest()
                    .withName(UUID.randomUUID().toString())
                    .withInput(stateMachineInput)
                    .withStateMachineArn(stepFunctionArn));
  }

  private Try<String, JsonProcessingException> getStepFunctionInput(final String jobConfigUri) {
    return Try.withCatch(
        () -> objectMapper.writeValueAsString(new StepFunctionInput(jobConfigUri)),
        JsonProcessingException.class);
  }
}
