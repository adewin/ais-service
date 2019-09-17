package uk.gov.ukho.ais.lambda.handleheatmapoutcome;

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
@TestPropertySource(properties = {"JOB_SUBMISSION_BUCKET_NAME=test-bucket"})
public class HandleHeatmapOutcomeLambdaApplicationTest {

  @Test
  public void whenFunctionIsLoadedThenSpringContextLoads() {}
}
