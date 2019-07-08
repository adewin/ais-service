package uk.gov.ukho.ais.s3eventhandling.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.s3.event.S3EventNotification;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import uk.gov.ukho.ais.s3eventhandling.model.S3Object;
import uk.gov.ukho.ais.s3eventhandling.model.S3ObjectEvent;
import uk.gov.ukho.ais.s3testutil.S3EventUtil;

public class S3ObjectExtractorTest {

  @Test
  public void whenExtractingS3ObjectsFromS3EventThenListOfS3ObjectsReturned() {
    final String bucketName = "ukho-data-bucket";
    final String objectName = "ais-data.txt";
    final String urlEncodedObject = "ais+data.txt";

    final List<S3EventNotification.S3EventNotificationRecord> records =
        Arrays.asList(
            S3EventUtil.createRecordFor(
                bucketName, objectName, S3ObjectEvent.CREATED.getEvents().get(0)),
            S3EventUtil.createRecordFor(
                bucketName, urlEncodedObject, S3ObjectEvent.CREATED.getEvents().get(0)),
            S3EventUtil.createRecordFor(
                bucketName, "NotInteresting.txt", S3ObjectEvent.REMOVED.getEvents().get(0)));

    final List<S3Object> result = S3ObjectExtractor.extractS3Objects(new S3Event(records));

    assertThat(result)
        .usingFieldByFieldElementComparator()
        .containsExactlyInAnyOrder(
            new S3Object(bucketName, objectName, S3ObjectEvent.CREATED),
            new S3Object(bucketName, "ais data.txt", S3ObjectEvent.CREATED),
            new S3Object(bucketName, "NotInteresting.txt", S3ObjectEvent.REMOVED));
  }
}
