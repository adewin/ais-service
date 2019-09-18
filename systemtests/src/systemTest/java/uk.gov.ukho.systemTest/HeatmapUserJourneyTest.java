package uk.gov.ukho.systemTest;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import geotrellis.raster.io.geotiff.SinglebandGeoTiff;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.assertj.core.api.SoftAssertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import scala.Tuple2;

public class HeatmapUserJourneyTest {

  private final String inputJobSubmissionBucketName = "ukho-heatmap-job-submission";
  private final String inputSqlFileBucketName = "ukho-heatmap-sql-archive";
  private final String sqlFileName = "system-test.sql";
  private final String outputBucketName = "ukho-heatmap-data";
  private final String testNovJobConfigFileName = "system-test-nov.json";
  private final String testDecJobConfigFileName = "system-test-dec.json";
  private final String testJanJobConfigFileName = "system-test-jan.json";
  private final String expectedSeasonal6Hr30KmHeatmapKey =
      "sqlFilename=system-test.sql.1/resample=6hr-30km/type=seasonal/"
          + "year=2018/season=winter/system-test.sql.1-1km-res-6hr-30km-winter-2018.tif";
  private final String expectedJan6Hr30KmHeatmapKey =
      "sqlFilename=system-test.sql.1/resample=6hr-30km/type=monthly/"
          + "year=2019/month=1/system-test.sql.1-1km-res-6hr-30km-monthly-Jan-2019.tif";
  private final String expectedDec6Hr30KmHeatmapKey =
      "sqlFilename=system-test.sql.1/resample=6hr-30km/type=monthly/"
          + "year=2018/month=12/system-test.sql.1-1km-res-6hr-30km-monthly-Dec-2018.tif";
  private final String expectedNov6Hr30KmHeatmapKey =
      "sqlFilename=system-test.sql.1/resample=6hr-30km/type=monthly/"
          + "year=2018/month=11/system-test.sql.1-1km-res-6hr-30km-monthly-Nov-2018.tif";
  private final String expectedSeasonal18Hr100KmHeatmapKey =
      "sqlFilename=system-test.sql.1/resample=18hr-100km/type=seasonal/"
          + "year=2018/season=winter/system-test.sql.1-1km-res-18hr-100km-winter-2018.tif";
  private final String expectedJan18Hr100KmHeatmapKey =
      "sqlFilename=system-test.sql.1/resample=18hr-100km/type=monthly/"
          + "year=2019/month=1/system-test.sql.1-1km-res-18hr-100km-monthly-Jan-2019.tif";
  private final String expectedDec18Hr100KmHeatmapKey =
      "sqlFilename=system-test.sql.1/resample=18hr-100km/type=monthly/"
          + "year=2018/month=12/system-test.sql.1-1km-res-18hr-100km-monthly-Dec-2018.tif";
  private final String expectedNov18Hr100KmHeatmapKey =
      "sqlFilename=system-test.sql.1/resample=18hr-100km/type=monthly/"
          + "year=2018/month=11/system-test.sql.1-1km-res-18hr-100km-monthly-Nov-2018.tif";

  private final AmazonS3 s3Client = AmazonS3ClientBuilder.defaultClient();

  @Before
  public void setup() {
    teardown();
  }

  @Test
  public void whenMonthlySqlFilesAreIngestedThenSeasonalHeatmapProduced() throws Exception {
    uploadFiles();

    waitUntilSeasonalHeatmapsHaveBeenGenerated();

    final File downloadedSeasonalHeatmap = download6Hr30KmSeasonalHeatmap();

    SoftAssertions.assertSoftly(
        softly -> {
          assertThatSeasonalHeatmapsAtBothResampleParametersProduced(softly);

          assertThatSeasonalGeoTiffProducedCorrectly(downloadedSeasonalHeatmap, softly);

          assertThatRequestedMonthlyHeatmapsProduced(softly);

          assertThatAllJobsHaveAnOutputFile(softly);
        });
  }

  private void uploadFiles() {
    uploadFile(inputSqlFileBucketName, "", sqlFileName);

    waitWhile(
        () -> !objectExists(inputSqlFileBucketName, "archive/" + sqlFileName + ".1"),
        Duration.ofMinutes(20));

    uploadFile(inputJobSubmissionBucketName, "submit/", testNovJobConfigFileName);
    uploadFile(inputJobSubmissionBucketName, "submit/", testDecJobConfigFileName);
    uploadFile(inputJobSubmissionBucketName, "submit/", testJanJobConfigFileName);
  }

  private void waitUntilSeasonalHeatmapsHaveBeenGenerated() {
    waitWhile(
        () ->
            !objectExists(outputBucketName, expectedSeasonal6Hr30KmHeatmapKey)
                && !objectExists(outputBucketName, expectedSeasonal18Hr100KmHeatmapKey),
        Duration.ofHours(12));
  }

  private void assertThatSeasonalHeatmapsAtBothResampleParametersProduced(SoftAssertions softly) {
    softly.assertThat(objectExists(outputBucketName, expectedSeasonal6Hr30KmHeatmapKey)).isTrue();
    softly.assertThat(objectExists(outputBucketName, expectedSeasonal18Hr100KmHeatmapKey)).isTrue();
  }

  private void assertThatAllJobsHaveAnOutputFile(final SoftAssertions softly) {
    softly.assertThat(hasOutcomeFileFor(testJanJobConfigFileName)).isTrue();
    softly.assertThat(hasOutcomeFileFor(testDecJobConfigFileName)).isTrue();
    softly.assertThat(hasOutcomeFileFor(testNovJobConfigFileName)).isTrue();
  }

  private void assertThatRequestedMonthlyHeatmapsProduced(final SoftAssertions softly) {
    softly.assertThat(objectExists(outputBucketName, expectedNov6Hr30KmHeatmapKey)).isTrue();
    softly.assertThat(objectExists(outputBucketName, expectedDec6Hr30KmHeatmapKey)).isTrue();
    softly.assertThat(objectExists(outputBucketName, expectedJan6Hr30KmHeatmapKey)).isTrue();

    softly.assertThat(objectExists(outputBucketName, expectedJan18Hr100KmHeatmapKey)).isTrue();
    softly.assertThat(objectExists(outputBucketName, expectedDec18Hr100KmHeatmapKey)).isTrue();
    softly.assertThat(objectExists(outputBucketName, expectedNov18Hr100KmHeatmapKey)).isTrue();
  }

  private void assertThatSeasonalGeoTiffProducedCorrectly(
      final File downloadedFile, final SoftAssertions softly) {
    SinglebandGeoTiff file = SinglebandGeoTiff.apply(downloadedFile.getAbsolutePath());

    Tuple2 minMax = file.tile().findMinMax();
    double min = (Integer) minMax._1();
    double max = (Integer) minMax._2();

    softly.assertThat(min).isZero();
    softly.assertThat(max).isNotZero();
  }

  @After
  public void teardown() {
    tryToDeleteObject(inputSqlFileBucketName, sqlFileName);
    tryToDeleteObject(inputSqlFileBucketName, "archive/" + sqlFileName + ".1");

    tryToDeleteObject(inputJobSubmissionBucketName, "submit/" + testJanJobConfigFileName);
    tryToDeleteObject(inputJobSubmissionBucketName, "submit/" + testDecJobConfigFileName);
    tryToDeleteObject(inputJobSubmissionBucketName, "submit/" + testNovJobConfigFileName);

    tryToDeleteOutcomeFilesForJobConfigFile(testJanJobConfigFileName);
    tryToDeleteOutcomeFilesForJobConfigFile(testDecJobConfigFileName);
    tryToDeleteOutcomeFilesForJobConfigFile(testNovJobConfigFileName);

    tryToDeleteObject(outputBucketName, expectedSeasonal6Hr30KmHeatmapKey);
    tryToDeleteObject(outputBucketName, expectedJan6Hr30KmHeatmapKey);
    tryToDeleteObject(outputBucketName, expectedDec6Hr30KmHeatmapKey);
    tryToDeleteObject(outputBucketName, expectedNov6Hr30KmHeatmapKey);

    tryToDeleteObject(outputBucketName, expectedSeasonal18Hr100KmHeatmapKey);
    tryToDeleteObject(outputBucketName, expectedJan18Hr100KmHeatmapKey);
    tryToDeleteObject(outputBucketName, expectedDec18Hr100KmHeatmapKey);
    tryToDeleteObject(outputBucketName, expectedNov18Hr100KmHeatmapKey);
  }

  private void tryToDeleteOutcomeFilesForJobConfigFile(final String testJanJobConfigFileName) {
    s3OutputFilesForJobConfig(testJanJobConfigFileName)
        .forEach(object -> tryToDeleteObject(inputJobSubmissionBucketName, object));
  }

  private List<String> s3OutputFilesForJobConfig(final String jobConfigFileName) {
    List<String> keys = new ArrayList<>();
    ListObjectsV2Result listObjectsV2Result;
    do {
      listObjectsV2Result = s3Client.listObjectsV2(inputJobSubmissionBucketName, "completed/");

      keys.addAll(
          listObjectsV2Result.getObjectSummaries().stream()
              .map(S3ObjectSummary::getKey)
              .filter(key -> key.contains("/input-file-name=" + jobConfigFileName + "/"))
              .collect(Collectors.toList()));

    } while (listObjectsV2Result.isTruncated());

    return keys;
  }

  private boolean hasOutcomeFileFor(final String jobConfigFileName) {
    return s3OutputFilesForJobConfig(jobConfigFileName).size() > 0;
  }

  private void tryToDeleteObject(final String bucket, final String objectKey) {
    try {
      s3Client.deleteObject(new DeleteObjectRequest(bucket, objectKey));
    } catch (final AmazonS3Exception amazonException) {
      System.out.println(
          "Could not delete: "
              + objectKey
              + " from: "
              + bucket
              + " because: "
              + amazonException.getMessage());
    }
  }

  private void uploadFile(
      final String bucketName, final String objectKeyPrefix, final String fileName) {

    final String filePath = getClass().getResource("/" + fileName).getPath();

    s3Client.putObject(bucketName, objectKeyPrefix + filePath, new File(filePath));
  }

  private File download6Hr30KmSeasonalHeatmap() throws IOException {
    Path tempDir = Files.createTempDirectory("ukho-heatmap-data");
    File localFile = new File(tempDir.toFile(), "seasonal-6hr-30km.tif");

    s3Client.getObject(
        new GetObjectRequest(outputBucketName, expectedSeasonal6Hr30KmHeatmapKey), localFile);

    return localFile;
  }

  private boolean objectExists(final String s3BucketName, final String keyName) {
    return s3Client.doesObjectExist(s3BucketName, keyName);
  }

  private void waitWhile(Supplier<Boolean> predicate, Duration maximumDuration) {
    Instant startTime = Instant.now();
    while (predicate.get()) {
      Instant currentTime = Instant.now();
      Duration runDuration = Duration.between(startTime, currentTime);

      if (runDuration.compareTo(maximumDuration) >= 0) {
        throw new RuntimeException("While waiting the run duration exceeded the maximum duration");
      }

      sleep(30);
    }
  }

  private void sleep(final long seconds) {
    try {
      Thread.sleep(seconds * 1000);
    } catch (Exception ignored) {
    }
  }
}
