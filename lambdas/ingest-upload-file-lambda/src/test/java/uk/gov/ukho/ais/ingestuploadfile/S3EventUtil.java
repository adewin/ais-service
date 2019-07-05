package uk.gov.ukho.ais.ingestuploadfile;

import com.amazonaws.services.s3.event.S3EventNotification;
import java.time.Instant;

public class S3EventUtil {

  public static S3EventNotification.S3EventNotificationRecord createRecordFor(
      final String bucketName, final String objectKey, final String eventName) {
    return new S3EventNotification.S3EventNotificationRecord(
        "eu-west-1",
        eventName,
        "aws:s3",
        Instant.now().toString(),
        "2.0",
        new S3EventNotification.RequestParametersEntity("0.0.0.0"),
        new S3EventNotification.ResponseElementsEntity("325345", "029-193"),
        new S3EventNotification.S3Entity(
            "234",
            new S3EventNotification.S3BucketEntity(bucketName, null, "arn:s3:::" + bucketName),
            new S3EventNotification.S3ObjectEntity(objectKey, 12L, "", "", ""),
            "1.0"),
        new S3EventNotification.UserIdentityEntity("4rufn390"),
        null);
  }
}
