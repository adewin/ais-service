package uk.gov.ukho.systemTest;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

public class FirstTest {

    private String bucketName = "ukho-mining-diamonds";

    @Before
    public void setup() {
        //Check table with test data is avalible using athena client

    }

    @Test
    public void testName() {
        //Upload SQL file with test prefix (Need to maintain data integrity)

        Regions clientRegion = Regions.EU_WEST_2;

//        System.out.println("root: " + getClass().getResource("/").getPath());

        String s = getClass().getResource("/test.sql").getPath();

//        System.out.println(s);

        AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                .withRegion(clientRegion)
                .build();


        s3Client.putObject(
                bucketName,
                "test.sql",
                new File(s)
        );
    }
    //Poll expected output bucket for .tiff

    //General assertions from reading tiff


    @After
    public void teardown() {

    }
}
