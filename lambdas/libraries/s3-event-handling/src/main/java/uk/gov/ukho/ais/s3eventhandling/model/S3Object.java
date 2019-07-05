package uk.gov.ukho.ais.s3eventhandling.model;

public class S3Object {

  private final String bucket;
  private final String key;
  private final S3ObjectEvent s3ObjectEvent;

  public S3Object(String bucket, String key, S3ObjectEvent s3ObjectEvent) {
    this.bucket = bucket;
    this.key = key;
    this.s3ObjectEvent = s3ObjectEvent;
  }

  public String getBucket() {
    return bucket;
  }

  public String getKey() {
    return key;
  }

  public S3ObjectEvent getS3ObjectEvent() {
    return s3ObjectEvent;
  }

  @Override
  public String toString() {
    return "s3a://" + bucket + "/" + key;
  }
}
