package uk.gov.ukho.ais.s3eventhandling.service;

import com.amazonaws.services.lambda.runtime.events.S3Event;
import java.util.List;
import java.util.stream.Collectors;
import uk.gov.ukho.ais.s3eventhandling.model.S3Object;
import uk.gov.ukho.ais.s3eventhandling.model.S3ObjectEvent;

public final class S3ObjectExtractor {

  private S3ObjectExtractor() {}

  public static List<S3Object> extractS3Objects(final S3Event s3Event) {
    return s3Event.getRecords().stream()
        .map(
            s3EventNotificationRecord ->
                new S3Object(
                    s3EventNotificationRecord.getS3().getBucket().getName(),
                    s3EventNotificationRecord.getS3().getObject().getKey(),
                    S3ObjectEvent.forEventName(s3EventNotificationRecord.getEventName())))
        .collect(Collectors.toList());
  }
}
