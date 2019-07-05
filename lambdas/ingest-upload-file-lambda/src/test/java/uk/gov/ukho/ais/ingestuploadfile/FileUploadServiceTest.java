package uk.gov.ukho.ais.ingestuploadfile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.amazonaws.services.lambda.runtime.events.S3Event;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.assertj.core.groups.Tuple;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.ukho.ais.ingestuploadfile.s3.S3ObjectCopier;
import uk.gov.ukho.ais.ingestuploadfile.service.FileUploadService;
import uk.gov.ukho.ais.s3eventhandling.model.S3Object;
import uk.gov.ukho.ais.s3eventhandling.model.S3ObjectEvent;
import uk.gov.ukho.ais.s3testutil.S3EventUtil;

@RunWith(MockitoJUnitRunner.class)
public class FileUploadServiceTest {

  private final String putCreationEvent = S3ObjectEvent.CREATED.getEvents().get(0);

  @Mock private S3ObjectCopier mockS3ObjectCopier;

  private FileUploadService fileUploadService;

  @Before
  public void before() {
    MockitoAnnotations.initMocks(this);
    fileUploadService = new FileUploadService(mockS3ObjectCopier);
  }

  @Test
  public void whenNoObjectsGivenThenNoFilesCopied() {
    final S3Event s3Event = new S3Event(Collections.emptyList());

    boolean result = fileUploadService.copyFiles(s3Event);

    assertThat(result).isTrue();

    verify(mockS3ObjectCopier, never()).copyS3ObjectToNewLocation(any());
  }

  @Test
  public void whenNewFileUploadedThenCopiedToRawBucket() {
    final String objectKey = "key";

    final S3Object s3Object = new S3Object("bucket", objectKey, S3ObjectEvent.CREATED);

    final S3Event s3Event =
        new S3Event(
            Collections.singletonList(
                S3EventUtil.createRecordFor("bucket", objectKey, putCreationEvent)));

    when(mockS3ObjectCopier.copyS3ObjectToNewLocation(any(S3Object.class))).thenReturn(true);

    fileUploadService.copyFiles(s3Event);

    ArgumentCaptor<S3Object> argumentCaptor = ArgumentCaptor.forClass(S3Object.class);

    verify(mockS3ObjectCopier, times(1)).copyS3ObjectToNewLocation(argumentCaptor.capture());

    List<S3Object> capturedArguments = argumentCaptor.getAllValues();

    assertThat(capturedArguments).usingFieldByFieldElementComparator().containsExactly(s3Object);
  }

  @Test
  public void whenMultipleFilesUploadedThenCopiedToRawBucket() {
    final String objectKey1 = "key1";
    final String objectKey2 = "key2";

    final String bucket = "bucket";

    final S3Event s3Event =
        new S3Event(
            Arrays.asList(
                S3EventUtil.createRecordFor(bucket, objectKey1, putCreationEvent),
                S3EventUtil.createRecordFor(bucket, objectKey2, putCreationEvent)));

    when(mockS3ObjectCopier.copyS3ObjectToNewLocation(any(S3Object.class))).thenReturn(true);

    fileUploadService.copyFiles(s3Event);

    ArgumentCaptor<S3Object> argumentCaptor = ArgumentCaptor.forClass(S3Object.class);

    verify(mockS3ObjectCopier, times(2)).copyS3ObjectToNewLocation(argumentCaptor.capture());

    List<S3Object> capturedArguments = argumentCaptor.getAllValues();

    assertThat(capturedArguments)
        .extracting("bucket", "key", "s3ObjectEvent")
        .containsExactlyInAnyOrder(
            Tuple.tuple(bucket, objectKey1, S3ObjectEvent.CREATED),
            Tuple.tuple(bucket, objectKey2, S3ObjectEvent.CREATED));
  }

  @Test
  public void whenTwoFilesUploadedAndOneFailsToBeUploadedButOtherIsUploadedThenReturnsFalse() {
    final String objectKey1 = "key1";
    final String objectKey2 = "key2";

    final String bucket = "bucket";

    final S3Event s3Event =
        new S3Event(
            Arrays.asList(
                S3EventUtil.createRecordFor(bucket, objectKey1, putCreationEvent),
                S3EventUtil.createRecordFor(bucket, objectKey2, putCreationEvent)));

    when(mockS3ObjectCopier.copyS3ObjectToNewLocation(any(S3Object.class)))
        .thenReturn(true)
        .thenReturn(false);

    boolean result = fileUploadService.copyFiles(s3Event);

    assertThat(result).isFalse();

    ArgumentCaptor<S3Object> argumentCaptor = ArgumentCaptor.forClass(S3Object.class);

    verify(mockS3ObjectCopier, times(2)).copyS3ObjectToNewLocation(argumentCaptor.capture());

    List<S3Object> capturedArguments = argumentCaptor.getAllValues();

    assertThat(capturedArguments)
        .extracting("bucket", "key", "s3ObjectEvent")
        .containsExactlyInAnyOrder(
            Tuple.tuple(bucket, objectKey1, S3ObjectEvent.CREATED),
            Tuple.tuple(bucket, objectKey2, S3ObjectEvent.CREATED));
  }

  @Test
  public void whenNoCreationRecordsThenNoFilesCopied() {
    final String objectKey1 = "key1";
    final String objectKey2 = "key2";

    final String bucket = "bucket";
    final String removedEvent = S3ObjectEvent.REMOVED.getEvents().get(0);

    final S3Event s3Event =
        new S3Event(
            Arrays.asList(
                S3EventUtil.createRecordFor(bucket, objectKey1, removedEvent),
                S3EventUtil.createRecordFor(bucket, objectKey2, removedEvent)));

    boolean result = fileUploadService.copyFiles(s3Event);

    assertThat(result).isTrue();

    verify(mockS3ObjectCopier, never()).copyS3ObjectToNewLocation(any());
  }
}
