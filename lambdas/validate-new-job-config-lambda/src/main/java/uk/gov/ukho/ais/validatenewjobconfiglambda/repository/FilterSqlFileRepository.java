package uk.gov.ukho.ais.validatenewjobconfiglambda.repository;

import com.amazonaws.services.s3.AmazonS3;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class FilterSqlFileRepository {

  private final String bucketName;
  private final String prefix;

  private final AmazonS3 s3client;

  public FilterSqlFileRepository(
      @Value("${archive.bucketName}") final String bucketName,
      @Value("${archive.prefix}") final String prefix,
      final AmazonS3 s3client) {
    this.bucketName = bucketName;
    this.prefix = prefix;
    this.s3client = s3client;
  }

  public boolean exists(final String fileName) {
    return s3client.doesObjectExist(bucketName, prefix + "/" + fileName);
  }

  public String getS3Uri(final String fileName) {
    return "s3://" + bucketName + "/" + prefix + "/" + fileName;
  }
}
