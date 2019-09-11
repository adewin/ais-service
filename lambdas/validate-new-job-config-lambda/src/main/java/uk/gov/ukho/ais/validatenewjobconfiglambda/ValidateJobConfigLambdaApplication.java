package uk.gov.ukho.ais.validatenewjobconfiglambda;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.function.context.FunctionalSpringApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ValidateJobConfigLambdaApplication {

  public static void main(String[] args) {
    FunctionalSpringApplication.run(ValidateJobConfigLambdaApplication.class, args);
  }

  @Bean
  public AmazonS3 getAmazonS3Client() {
    return AmazonS3ClientBuilder.defaultClient();
  }
}
