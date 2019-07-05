package uk.gov.ukho.ais.s3eventhandling.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import uk.gov.ukho.ais.s3eventhandling.model.S3Object;
import uk.gov.ukho.ais.s3eventhandling.model.S3ObjectEvent;

public class S3NotificationFilterTest {

  private final S3Object createdObject = new S3Object("bucket", "key", S3ObjectEvent.CREATED);

  @Test
  public void whenRecordIsForCreationEventThenGeneratedPredicateReturnsTrue() {
    boolean result =
        S3NotificationFilter.filterObjectsForEventType(S3ObjectEvent.CREATED).test(createdObject);

    assertThat(result).isTrue();
  }

  @Test
  public void whenRecordIsNotForCreationEventThenGeneratedPredicateReturnsFalse() {
    boolean result =
        S3NotificationFilter.filterObjectsForEventType(S3ObjectEvent.REMOVED).test(createdObject);

    assertThat(result).isFalse();
  }
}
