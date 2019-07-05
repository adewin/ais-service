package uk.gov.ukho.ais.ingestuploadfile.service;

import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import java.util.function.Supplier;
import uk.gov.ukho.ais.ingestuploadfile.s3.S3ObjectCopier;

public class FileUploadServiceSupplier implements Supplier<FileUploadService> {

  private static final String DESTINATION_BUCKET = System.getenv("DESTINATION_BUCKET");
  private static final String DESTINATION_KEY_PREFIX = System.getenv("DESTINATION_KEY_PREFIX");

  private static S3ObjectCopier s3ObjectCopierInstance = null;

  @Override
  public FileUploadService get() {
    return new FileUploadService(getS3ObjectCopierInstance());
  }

  private static S3ObjectCopier getS3ObjectCopierInstance() {
    if (s3ObjectCopierInstance == null) {
      s3ObjectCopierInstance =
          new S3ObjectCopier(
              AmazonS3ClientBuilder.defaultClient(), DESTINATION_BUCKET, DESTINATION_KEY_PREFIX);
    }
    return s3ObjectCopierInstance;
  }
}
