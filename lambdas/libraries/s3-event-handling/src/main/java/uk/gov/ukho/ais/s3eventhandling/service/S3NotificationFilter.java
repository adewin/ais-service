package uk.gov.ukho.ais.s3eventhandling.service;

import java.util.function.Predicate;
import uk.gov.ukho.ais.s3eventhandling.model.S3Object;
import uk.gov.ukho.ais.s3eventhandling.model.S3ObjectEvent;

public class S3NotificationFilter {

  public static Predicate<S3Object> filterObjectsForEventType(final S3ObjectEvent s3ObjectEvent) {
    return s3Object -> s3Object.getS3ObjectEvent() == s3ObjectEvent;
  }
}
