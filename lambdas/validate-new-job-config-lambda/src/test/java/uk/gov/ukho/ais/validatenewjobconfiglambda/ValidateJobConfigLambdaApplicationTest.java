package uk.gov.ukho.ais.validatenewjobconfiglambda;

import com.amazonaws.services.s3.AmazonS3;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@MockBean(AmazonS3.class)
@TestPropertySource(
    properties = {"FILTER_SQL_ARCHIVE_BUCKET_NAME=bucket", "FILTER_SQL_ARCHIVE_PREFIX=prefix"})
public class ValidateJobConfigLambdaApplicationTest {

  @Test
  public void applicationContextLoads() {}
}
