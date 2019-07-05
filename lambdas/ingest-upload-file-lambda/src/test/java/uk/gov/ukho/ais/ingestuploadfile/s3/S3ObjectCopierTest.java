package uk.gov.ukho.ais.ingestuploadfile.s3;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.ukho.ais.ingestuploadfile.model.S3Object;
import uk.gov.ukho.ais.ingestuploadfile.model.S3ObjectEvent;

@RunWith(MockitoJUnitRunner.class)
public class S3ObjectCopierTest {

  private final String outputDestination = "destination";

  @Mock private AmazonS3 mockS3;

  private S3ObjectCopier s3ObjectCopier;

  @Before
  public void before() {
    s3ObjectCopier = new S3ObjectCopier(mockS3, outputDestination, "data/");
  }

  @Test
  public void whenCopyingObjectThenObjectCopiedToNewLocation() {
    final S3Object s3Object = new S3Object("bucket", "key", S3ObjectEvent.CREATED);

    boolean result = s3ObjectCopier.copyS3ObjectToNewLocation(s3Object);

    assertThat(result).isTrue();

    verify(mockS3, times(1))
        .copyObject(s3Object.getBucket(), s3Object.getKey(), outputDestination, "data/key");
  }

  @Test
  public void
      whenCopyingObjectThatIsNotInTheRootOfBucketThenPathDisregardedAndCopiedIntoDataDirectory() {
    final S3Object s3Object = new S3Object("bucket", "not/at/root/key", S3ObjectEvent.CREATED);

    boolean result = s3ObjectCopier.copyS3ObjectToNewLocation(s3Object);

    assertThat(result).isTrue();

    verify(mockS3, times(1))
        .copyObject(s3Object.getBucket(), s3Object.getKey(), outputDestination, "data/key");
  }

  @Test
  public void whenCopyingObjectDoesNotSucceedThenReturnsFalse() {
    final S3Object s3Object = new S3Object("bucket", "does/not/exist/key", S3ObjectEvent.CREATED);

    doThrow(new AmazonServiceException("Object does not exist"))
        .when(mockS3)
        .copyObject(s3Object.getBucket(), s3Object.getKey(), outputDestination, "data/key");

    boolean result = s3ObjectCopier.copyS3ObjectToNewLocation(s3Object);

    assertThat(result).isFalse();

    verify(mockS3, times(1))
        .copyObject(s3Object.getBucket(), s3Object.getKey(), outputDestination, "data/key");
  }
}
