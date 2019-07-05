package uk.gov.ukho.ais.s3eventhandling.model;

import java.util.Arrays;
import java.util.List;

public enum S3ObjectEvent {
  CREATED(
      "ObjectCreated:Put",
      "ObjectCreated:Post",
      "ObjectCreated:Copy",
      "ObjectCreated:CompleteMultipartUpload"),
  REMOVED("ObjectRemoved:Delete", "ObjectRemoved:DeleteMarkerCreated"),
  RESTORE("ObjectRestore:Post", "ObjectRestore:Completed"),
  REDUCED_REDUNDANCY_LOST_OBJECT("ReducedRedundancyLostObject"),
  UNKNOWN();

  private final List<String> events;

  S3ObjectEvent(String... events) {
    this.events = Arrays.asList(events);
  }

  public List<String> getEvents() {
    return events;
  }

  public static S3ObjectEvent forEventName(final String eventName) {
    return Arrays.stream(S3ObjectEvent.values())
        .filter(event -> event.events.contains(eventName))
        .findFirst()
        .orElse(S3ObjectEvent.UNKNOWN);
  }
}
