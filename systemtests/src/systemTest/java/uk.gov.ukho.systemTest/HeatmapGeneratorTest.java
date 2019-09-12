package uk.gov.ukho.systemTest;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import geotrellis.raster.io.geotiff.SinglebandGeoTiff;
import geotrellis.raster.io.geotiff.reader.GeoTiffReader;
import geotrellis.spark.io.s3.util.S3RangeReader;
import geotrellis.util.StreamingByteReader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import scala.Tuple2;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

public class HeatmapGeneratorTest {

    private final String bucketName = "ukho-mining-diamonds";
    private final String testDecSqlFileName = "";
    private final String testJanSqlFileName = "";
    private final String testNovSqlFileName = "";
    private final String expectedSeasonalHeatmapName = "";

    private Regions defaultRegion = Regions.EU_WEST_2;

    @Before
    public void setup() {
        teardown();
    }

    @Test
    public void whenMonthlySqlFilesAreIngestedThenSeasonalHeatmapProduced() throws Exception {
        AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                .withRegion(defaultRegion)
                .build();

        uploadObject(testNovSqlFileName, s3Client);
        uploadObject(testDecSqlFileName, s3Client);
        uploadObject(testJanSqlFileName, s3Client);

        final File downloadedFile = downloadObject(bucketName, expectedSeasonalHeatmapName, s3Client);

        SinglebandGeoTiff file = SinglebandGeoTiff.apply(downloadedFile.getAbsolutePath());

        Tuple2 minMax = file.tile().findMinMax();
        double min = (Integer) minMax._1();
        double max = (Integer) minMax._2();

        assertThat(min).isNotZero();
        assertThat(max).isNotZero();
    }


    @After
    public void teardown() {
        //delete stuff
    }


    private void uploadObject(final String fileName, final AmazonS3 s3Client) {
        final String filePath = getClass().getResource("/" + fileName).getPath();

        s3Client.putObject(
                bucketName,
                filePath,
                new File(filePath)
        );
    }

    private File downloadObject(final String s3BucketName, final String keyName, final AmazonS3 s3Client) throws IOException {
        Path tempDir = Files.createTempDirectory(s3BucketName);
        File localFile = new File(tempDir.toFile(), keyName);

        waitWhile(() -> !objectExists(s3BucketName, keyName, s3Client), Duration.ofHours(12));

        s3Client.getObject(new GetObjectRequest(s3BucketName, keyName), localFile);

        return localFile;
    }

    private boolean objectExists(final String s3BucketName, final String keyName, final AmazonS3 s3Client) {
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

