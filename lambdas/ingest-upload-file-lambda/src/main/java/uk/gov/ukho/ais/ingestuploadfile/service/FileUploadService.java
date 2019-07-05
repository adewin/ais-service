package uk.gov.ukho.ais.ingestuploadfile.service;

import com.amazonaws.services.lambda.runtime.events.S3Event;
import uk.gov.ukho.ais.ingestuploadfile.s3.S3ObjectCopier;
import uk.gov.ukho.ais.s3eventhandling.model.S3ObjectEvent;
import uk.gov.ukho.ais.s3eventhandling.service.S3NotificationFilter;
import uk.gov.ukho.ais.s3eventhandling.service.S3ObjectExtractor;

public class FileUploadService {

  private S3ObjectCopier s3ObjectCopier;

  public FileUploadService(S3ObjectCopier s3ObjectCopier) {
    this.s3ObjectCopier = s3ObjectCopier;
  }

  public Boolean copyFiles(final S3Event input) {
    return S3ObjectExtractor.extractS3Objects(input).stream()
        .filter(S3NotificationFilter.filterObjectsForEventType(S3ObjectEvent.CREATED))
        .allMatch(s3ObjectCopier::copyS3ObjectToNewLocation);
  }
}
