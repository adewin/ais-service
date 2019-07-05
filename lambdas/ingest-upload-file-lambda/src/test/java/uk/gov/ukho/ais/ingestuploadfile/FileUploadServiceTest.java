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
import uk.gov.ukho.ais.ingestuploadfile.model.S3Object;
import uk.gov.ukho.ais.ingestuploadfile.model.S3ObjectEvent;
import uk.gov.ukho.ais.ingestuploadfile.s3.S3ObjectCopier;
import uk.gov.ukho.ais.ingestuploadfile.s3.S3ObjectExtractor;
import uk.gov.ukho.ais.ingestuploadfile.service.FileUploadService;

@RunWith(MockitoJUnitRunner.class)
public class FileUploadServiceTest {

  private final String putCreationEvent = S3ObjectEvent.CREATED.getEvents().get(0);

  @Mock private S3ObjectCopier mockS3ObjectCopier;

  @Mock private S3ObjectExtractor mockS3ObjectExtractor;

  private FileUploadService fileUploadService;

  @Before
  public void before() {
    MockitoAnnotations.initMocks(this);
    fileUploadService = new FileUploadService(mockS3ObjectExtractor, mockS3ObjectCopier);
  }

  @Test
  public void whenNoObjectsGivenThenNoFilesCopied() {
    final S3Event s3Event = new S3Event(Collections.emptyList());

    when(mockS3ObjectExtractor.extractS3Objects(s3Event)).thenReturn(Collections.emptyList());

    boolean result = fileUploadService.copyFiles(s3Event);

    assertThat(result).isTrue();

    verify(mockS3ObjectExtractor, times(1)).extractS3Objects(s3Event);
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

    when(mockS3ObjectExtractor.extractS3Objects(s3Event))
        .thenReturn(Collections.singletonList(s3Object));
    when(mockS3ObjectCopier.copyS3ObjectToNewLocation(any(S3Object.class))).thenReturn(true);

    fileUploadService.copyFiles(s3Event);

    ArgumentCaptor<S3Object> argumentCaptor = ArgumentCaptor.forClass(S3Object.class);

    verify(mockS3ObjectExtractor, times(1)).extractS3Objects(s3Event);
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

    when(mockS3ObjectExtractor.extractS3Objects(s3Event))
        .thenReturn(
            Arrays.asList(
                new S3Object(bucket, objectKey1, S3ObjectEvent.CREATED),
                new S3Object(bucket, objectKey2, S3ObjectEvent.CREATED)));
    when(mockS3ObjectCopier.copyS3ObjectToNewLocation(any(S3Object.class))).thenReturn(true);

    fileUploadService.copyFiles(s3Event);

    ArgumentCaptor<S3Object> argumentCaptor = ArgumentCaptor.forClass(S3Object.class);

    verify(mockS3ObjectExtractor, times(1)).extractS3Objects(s3Event);
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

    when(mockS3ObjectExtractor.extractS3Objects(s3Event))
        .thenReturn(
            Arrays.asList(
                new S3Object(bucket, objectKey1, S3ObjectEvent.CREATED),
                new S3Object(bucket, objectKey2, S3ObjectEvent.CREATED)));
    when(mockS3ObjectCopier.copyS3ObjectToNewLocation(any(S3Object.class)))
        .thenReturn(true)
        .thenReturn(false);

    boolean result = fileUploadService.copyFiles(s3Event);

    assertThat(result).isFalse();

    ArgumentCaptor<S3Object> argumentCaptor = ArgumentCaptor.forClass(S3Object.class);

    verify(mockS3ObjectExtractor, times(1)).extractS3Objects(s3Event);
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

    final S3Event s3Event =
        new S3Event(
            Arrays.asList(
                S3EventUtil.createRecordFor(bucket, objectKey1, putCreationEvent),
                S3EventUtil.createRecordFor(bucket, objectKey2, putCreationEvent)));

    when(mockS3ObjectExtractor.extractS3Objects(s3Event))
        .thenReturn(
            Arrays.asList(
                new S3Object(bucket, objectKey1, S3ObjectEvent.REMOVED),
                new S3Object(bucket, objectKey2, S3ObjectEvent.UNKNOWN)));

    boolean result = fileUploadService.copyFiles(s3Event);

    assertThat(result).isTrue();

    verify(mockS3ObjectExtractor, times(1)).extractS3Objects(s3Event);
    verify(mockS3ObjectCopier, never()).copyS3ObjectToNewLocation(any());
  }
}
