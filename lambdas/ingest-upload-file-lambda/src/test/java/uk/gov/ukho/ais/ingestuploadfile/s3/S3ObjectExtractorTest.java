package uk.gov.ukho.ais.ingestuploadfile.s3;

import static org.assertj.core.api.Assertions.assertThat;

import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.s3.event.S3EventNotification;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import uk.gov.ukho.ais.ingestuploadfile.S3EventUtil;
import uk.gov.ukho.ais.ingestuploadfile.model.S3Object;
import uk.gov.ukho.ais.ingestuploadfile.model.S3ObjectEvent;

public class S3ObjectExtractorTest {

  private final S3ObjectExtractor s3ObjectExtractor = new S3ObjectExtractor();

  @Test
  public void whenExtractingS3ObjectsFromS3EventThenListOfS3ObjectsReturned() {
    final String bucketName = "ukho-data-bucket";
    final String objectName = "ais-data.txt";

    final List<S3EventNotification.S3EventNotificationRecord> records =
        Arrays.asList(
            S3EventUtil.createRecordFor(
                bucketName, objectName, S3ObjectEvent.CREATED.getEvents().get(0)),
            S3EventUtil.createRecordFor(
                bucketName, "NotInteresting.txt", S3ObjectEvent.REMOVED.getEvents().get(0)));

    final List<S3Object> result = s3ObjectExtractor.extractS3Objects(new S3Event(records));

    assertThat(result)
        .usingFieldByFieldElementComparator()
        .containsExactlyInAnyOrder(
            new S3Object(bucketName, objectName, S3ObjectEvent.CREATED),
            new S3Object(bucketName, "NotInteresting.txt", S3ObjectEvent.REMOVED));
  }
}
