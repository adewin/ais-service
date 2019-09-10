package uk.gov.ukho.systemTest;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

public class HeatmapGeneratorTest {

    private final String bucketName = "ukho-mining-diamonds";
    private final String testDecSqlFile = "";
    private final String testJanSqlFile = "";
    private final String testFebSqlFile = "";

    private Regions defaultRegion = Regions.EU_WEST_2;

    @Before
    public void setup() {

    }

    @Test
    public void whenMonthlySqlFilesAreIngestedThenSeasonalHeatmapProduced() {
        //Upload SQL file with test prefix (Need to maintain data integrity)

        AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                .withRegion(defaultRegion)
                .build();

        uploadObject(testDecSqlFile, s3Client);
        uploadObject(testJanSqlFile, s3Client);
        uploadObject(testFebSqlFile, s3Client);

        downloadObject(bucketName, "KvnFull.png");

    }

    //General assertions from reading tiff


    @After
    public void teardown() {

    }

    private void uploadObject(final String fileName, final AmazonS3 s3Client) {

        final String filePath = getClass().getResource("/" + fileName).getPath();

        s3Client.putObject(
                bucketName,
                filePath,
                new File(filePath)
        );
    }

    private void downloadObject(final String s3BucketName, final String keyName) {
        AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                .withRegion(defaultRegion)
                .build();

        File localFile = new File("src/systemTest/resources/downloadedSeasonalTestFile");

        boolean fileExists = false;
        while (!fileExists) try {
            s3Client.getObject(new GetObjectRequest(s3BucketName, keyName), localFile);
            fileExists = true;

        } catch (Exception e) {
            continue;
        }
    }
}

