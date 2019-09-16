package uk.gov.ukho.ais.validatenewjobconfiglambda.repository;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import cyclops.control.Try;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class FilterSqlFileRepository {

  private final String bucketName;
  private final String prefix;

  private final AmazonS3 s3client;

  @Autowired
  public FilterSqlFileRepository(
      @Value("${FILTER_SQL_ARCHIVE_BUCKET_NAME}") final String bucketName,
      @Value("${FILTER_SQL_ARCHIVE_PREFIX}") final String prefix,
      final AmazonS3 s3client) {
    this.bucketName = bucketName;
    this.prefix = prefix;
    this.s3client = s3client;
  }

  public boolean exists(final String fileName) {
    return Try.withCatch(
            () -> s3client.getObjectMetadata(bucketName, prefix + "/" + fileName),
            AmazonS3Exception.class)
        .asEither()
        .fold(l -> false, r -> true);
  }

  public String getS3Uri(final String fileName) {
    return "s3://" + bucketName + "/" + prefix + "/" + fileName;
  }
}
