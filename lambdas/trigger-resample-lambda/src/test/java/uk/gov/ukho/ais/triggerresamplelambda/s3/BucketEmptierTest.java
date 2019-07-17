package uk.gov.ukho.ais.triggerresamplelambda.s3;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.ukho.ais.triggerresamplelambda.configuration.ResampleLambdaConfiguration;

@RunWith(MockitoJUnitRunner.class)
public class BucketEmptierTest {
  final String bucketName = "www.example.org";
  private final String outputLocation = "s3a://" + bucketName + "/";

  @Mock private AmazonS3 mockAmazonS3;
  @Mock private ResampleLambdaConfiguration mockResampleLambdaConfiguration;
  @Mock private ObjectListing mockObjectListing;
  @Captor private ArgumentCaptor<DeleteObjectsRequest> argumentCaptor;

  @InjectMocks private BucketEmptier bucketEmptier;

  @Test
  public void whenBucketWithASingleItemIsEmptiedThenTheBucketIsEmpty() {
    final S3ObjectSummary s3ObjectSummary = new S3ObjectSummary();
    final String objectKey = "theKey";

    s3ObjectSummary.setBucketName(bucketName);
    s3ObjectSummary.setKey(objectKey);

    when(mockResampleLambdaConfiguration.getOutputLocation()).thenReturn(outputLocation);
    when(mockAmazonS3.listObjects(bucketName)).thenReturn(mockObjectListing);
    when(mockAmazonS3.listNextBatchOfObjects(any(ObjectListing.class)))
        .thenReturn(mockObjectListing);
    when(mockObjectListing.getObjectSummaries())
        .thenReturn(Collections.singletonList(s3ObjectSummary));
    when(mockObjectListing.isTruncated()).thenReturn(false);

    bucketEmptier.emptyResampledBucket();

    verify(mockAmazonS3, times(1)).deleteObjects(argumentCaptor.capture());

    final DeleteObjectsRequest deleteObjectsRequest = argumentCaptor.getValue();
    assertThat(deleteObjectsRequest.getBucketName())
        .as("mock has received request to delete objects")
        .isEqualTo(bucketName);

    assertThat(deleteObjectsRequest.getKeys())
        .usingFieldByFieldElementComparator()
        .containsExactly(new DeleteObjectsRequest.KeyVersion(objectKey));
  }

  @Test
  public void whenBucketWithLargeNumberOfItemsIsEmptiedThenTheBucketIsEmpty() {

    final List<S3ObjectSummary> s3ObjectSummaries =
        IntStream.rangeClosed(0, 2)
            .boxed()
            .map(
                i -> {
                  final S3ObjectSummary s3ObjectSummary = new S3ObjectSummary();
                  s3ObjectSummary.setKey("key" + i);
                  s3ObjectSummary.setBucketName(bucketName);

                  return s3ObjectSummary;
                })
            .collect(Collectors.toList());

    when(mockResampleLambdaConfiguration.getOutputLocation()).thenReturn(outputLocation);

    when(mockAmazonS3.listObjects(bucketName)).thenReturn(mockObjectListing);
    when(mockAmazonS3.listNextBatchOfObjects(any(ObjectListing.class)))
        .thenReturn(mockObjectListing);

    when(mockObjectListing.getObjectSummaries())
        .thenReturn(
            Collections.singletonList(s3ObjectSummaries.get(0)),
            Collections.singletonList(s3ObjectSummaries.get(1)),
            Collections.singletonList(s3ObjectSummaries.get(2)));
    when(mockObjectListing.isTruncated()).thenReturn(true, true, false);

    bucketEmptier.emptyResampledBucket();

    verify(mockAmazonS3, times(3)).deleteObjects(argumentCaptor.capture());
    verify(mockObjectListing, times(3)).isTruncated();

    final List<DeleteObjectsRequest> allValues = argumentCaptor.getAllValues();

    assertThat(allValues)
        .as("mock has received requests to delete objects from correct bucket")
        .extracting("bucketName")
        .containsExactly(bucketName, bucketName, bucketName);

    assertThat(allValues)
        .as("mock has received requests to delete objects")
        .extracting(
            item ->
                item.getKeys().stream()
                    .map(DeleteObjectsRequest.KeyVersion::getKey)
                    .collect(Collectors.toList()))
        .containsExactly(
            Collections.singletonList("key0"),
            Collections.singletonList("key1"),
            Collections.singletonList("key2"));
  }

  @Test
  public void whenBucketIsAlreadyEmptyThenBucketNotEmptied() {
    when(mockResampleLambdaConfiguration.getOutputLocation()).thenReturn(outputLocation);

    when(mockAmazonS3.listNextBatchOfObjects(any(ObjectListing.class)))
        .thenReturn(mockObjectListing);
    when(mockAmazonS3.listObjects(bucketName)).thenReturn(mockObjectListing);

    when(mockObjectListing.getObjectSummaries()).thenReturn(Collections.emptyList());
    when(mockObjectListing.isTruncated()).thenReturn(false);

    bucketEmptier.emptyResampledBucket();

    verify(mockAmazonS3, never()).deleteObjects(any());
  }
}
