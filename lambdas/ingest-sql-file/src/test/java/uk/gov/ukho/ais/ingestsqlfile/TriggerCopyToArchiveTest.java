package uk.gov.ukho.ais.ingestsqlfile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.event.S3EventNotification;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import java.util.Collections;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(value = MockitoJUnitRunner.class)
public class TriggerCopyToArchiveTest {

  private static final String MY_BUCKET = "myBucket";
  private static final String MY_KEY = "myKey";
  private static final String ARCHIVE = "archive";

  @Mock private AmazonS3 amazonS3Mock;

  @Mock private ObjectListing objectListingMock;

  @Mock private S3Event s3EventMock;

  @Mock private S3EventNotification.S3EventNotificationRecord s3EventNotificationRecordMock;

  @Mock private S3EventNotification.S3Entity s3EntityMock;

  @Mock private S3EventNotification.S3BucketEntity s3BucketMock;

  @Mock private S3EventNotification.S3ObjectEntity s3ObjectMock;

  @Mock private S3ObjectSummary s3ObjectSummaryMock;

  @Before
  public void setup() {
    when(amazonS3Mock.listObjects(MY_BUCKET, ARCHIVE)).thenReturn(objectListingMock);
  }

  private void createEventMock(final String key) {
    when(s3EventMock.getRecords())
        .thenReturn(Collections.singletonList(s3EventNotificationRecordMock));
    when(s3EventNotificationRecordMock.getS3()).thenReturn(s3EntityMock);
    when(s3EntityMock.getBucket()).thenReturn(s3BucketMock);
    when(s3BucketMock.getName()).thenReturn(MY_BUCKET);
    when(s3EntityMock.getObject()).thenReturn(s3ObjectMock);
    when(s3ObjectMock.getKey()).thenReturn(key);
  }

  @Test
  public void whenGettingNextVersionForFileThatIsntThereThenVersionOneIsReturned() {
    createEventMock(MY_KEY);
    when(objectListingMock.getObjectSummaries()).thenReturn(Collections.emptyList());

    final TriggerCopyToArchive triggerCopyToArchive = new TriggerCopyToArchive(amazonS3Mock);
    final CopyToArchiveResult copyToArchiveResult = triggerCopyToArchive.apply(s3EventMock);

    verify(amazonS3Mock, times(1))
        .copyObject(MY_BUCKET, MY_KEY, MY_BUCKET, ARCHIVE + "/" + MY_KEY + ".1");

    assertThat(copyToArchiveResult.isSuccess()).isTrue();
    assertThat(copyToArchiveResult.getFileUri())
        .isEqualTo(MY_BUCKET + "/" + ARCHIVE + "/" + MY_KEY + ".1");
  }

  @Test
  public void whenGettingNextVersionForFileThatIsThereOnceThereThenVersionTwoIsReturned() {
    createEventMock(MY_KEY);
    when(objectListingMock.getObjectSummaries())
        .thenReturn(Collections.singletonList(s3ObjectSummaryMock));
    when(s3ObjectSummaryMock.getKey()).thenReturn(ARCHIVE + "/" + MY_KEY + ".1");

    final TriggerCopyToArchive triggerCopyToArchive = new TriggerCopyToArchive(amazonS3Mock);
    final CopyToArchiveResult copyToArchiveResult = triggerCopyToArchive.apply(s3EventMock);

    verify(amazonS3Mock, times(1))
        .copyObject(MY_BUCKET, MY_KEY, MY_BUCKET, ARCHIVE + "/" + MY_KEY + ".2");

    assertThat(copyToArchiveResult.isSuccess()).isTrue();
    assertThat(copyToArchiveResult.getFileUri())
        .isEqualTo(MY_BUCKET + "/" + ARCHIVE + "/" + MY_KEY + ".2");
  }

  @Test
  public void whenTriggeredWithCopiedArchiveFileThenNothingDone() {
    createEventMock(ARCHIVE + "/" + MY_KEY + ".2");

    final TriggerCopyToArchive triggerCopyToArchive = new TriggerCopyToArchive(amazonS3Mock);
    final CopyToArchiveResult copyToArchiveResult = triggerCopyToArchive.apply(s3EventMock);

    verify(amazonS3Mock, never())
        .copyObject(MY_BUCKET, MY_KEY, MY_BUCKET, ARCHIVE + "/" + MY_KEY + ".2");

    assertThat(copyToArchiveResult.isSuccess()).isFalse();
    assertThat(copyToArchiveResult.getFileUri())
        .isEqualTo(MY_BUCKET + "/" + ARCHIVE + "/" + MY_KEY + ".2");
  }
}
