package uk.gov.ukho.ais.ingestuploadfile.s3;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.gov.ukho.ais.ingestuploadfile.model.S3Object;

public class S3ObjectCopier {

  private static final Logger LOGGER = LogManager.getLogger(S3ObjectCopier.class);

  private final AmazonS3 amazonS3Client;
  private final String destinationBucket;
  private final String destinationKeyPrefix;

  public S3ObjectCopier(
      final AmazonS3 amazonS3Client,
      final String destinationBucket,
      final String destinationKeyPrefix) {
    this.amazonS3Client = amazonS3Client;
    this.destinationBucket = destinationBucket;
    this.destinationKeyPrefix = destinationKeyPrefix;
  }

  public boolean copyS3ObjectToNewLocation(final S3Object s3Object) {
    try {
      final String destinationKey = destinationKeyPrefix + getFileNameFromKey(s3Object.getKey());
      amazonS3Client.copyObject(
          s3Object.getBucket(), s3Object.getKey(), destinationBucket, destinationKey);
      return true;
    } catch (AmazonServiceException e) {
      LOGGER.error("Unable to copy file", e);
      return false;
    }
  }

  private String getFileNameFromKey(String key) {
    final String[] splitKey = key.split("/");

    return splitKey[splitKey.length - 1];
  }
}
