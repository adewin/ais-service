package uk.gov.ukho.ais.triggerresamplelambda.s3;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3URI;
import com.amazonaws.services.s3.model.DeleteObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import uk.gov.ukho.ais.triggerresamplelambda.configuration.ResampleLambdaConfiguration;

public class BucketEmptier {

  private AmazonS3 amazonS3Client;
  private ResampleLambdaConfiguration lambdaConfiguration;

  public BucketEmptier(
      final AmazonS3 amazonS3Client, final ResampleLambdaConfiguration lambdaConfiguration) {
    this.amazonS3Client = amazonS3Client;
    this.lambdaConfiguration = lambdaConfiguration;
  }

  public void emptyResampledBucket() {
    final String outputBucketName = getOutputBucketName();

    ObjectListing objectListing = amazonS3Client.listObjects(outputBucketName);

    do {
      final String[] objectsToDelete = getObjectsToDelete(objectListing);

      if (objectsToDelete.length > 0) {
        amazonS3Client.deleteObjects(
            new DeleteObjectsRequest(outputBucketName).withKeys(objectsToDelete));
      }

      objectListing = amazonS3Client.listNextBatchOfObjects(objectListing);
    } while (objectListing.isTruncated());
  }

  private String[] getObjectsToDelete(ObjectListing objectListing) {
    return objectListing.getObjectSummaries().stream()
        .map(S3ObjectSummary::getKey)
        .toArray(String[]::new);
  }

  private String getOutputBucketName() {
    final String s3Reference =
        lambdaConfiguration
            .getOutputLocation()
            .replace("s3a://", "s3://")
            .replace("s3n://", "s3://");
    return new AmazonS3URI(s3Reference).getBucket();
  }
}
