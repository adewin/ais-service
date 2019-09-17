package uk.gov.ukho.ais.ingestsqlfile;

import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.event.S3EventNotification;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class TriggerCopyToArchive implements Function<S3Event, CopyToArchiveResult> {

  private static final String ARCHIVE_FOLDER = "archive";
  private final AmazonS3 amazonS3;

  public TriggerCopyToArchive(final AmazonS3 amazonS3) {
    this.amazonS3 = amazonS3;
  }

  @Override
  public CopyToArchiveResult apply(final S3Event s3Event) {
    final S3EventNotification.S3Entity s3Entity = s3Event.getRecords().get(0).getS3();
    final String s3Bucket = s3Entity.getBucket().getName();
    final String s3Key = s3Entity.getObject().getKey();

    if (s3Key.startsWith(ARCHIVE_FOLDER + "/")) {
      return new CopyToArchiveResult(s3Bucket + "/" + s3Key, false);
    }

    final int nextVersion = getNextVersionForFile(s3Bucket, s3Key);
    String destinationKey = ARCHIVE_FOLDER + "/" + s3Key + "." + nextVersion;

    amazonS3.copyObject(s3Bucket, s3Key, s3Bucket, destinationKey);

    return new CopyToArchiveResult(s3Bucket + "/" + destinationKey, true);
  }

  private int getNextVersionForFile(final String s3Bucket, final String s3Key) {
    final ObjectListing objects = amazonS3.listObjects(s3Bucket, ARCHIVE_FOLDER);
    final Pattern archivedFilePattern =
        Pattern.compile("^" + ARCHIVE_FOLDER + "/" + s3Key + "\\.([0-9]+)$");

    return objects.getObjectSummaries().stream()
            .map(S3ObjectSummary::getKey)
            .filter(key -> archivedFilePattern.matcher(key).matches())
            .map(
                key -> {
                  final Matcher matcher = archivedFilePattern.matcher(key);
                  matcher.find();
                  return Integer.parseInt(matcher.group(1));
                })
            .max(Integer::compareTo)
            .orElse(0)
        + 1;
  }
}
