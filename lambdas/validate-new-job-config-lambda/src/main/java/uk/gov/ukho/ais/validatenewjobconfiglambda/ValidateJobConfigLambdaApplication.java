package uk.gov.ukho.ais.validatenewjobconfiglambda;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oath.cyclops.jackson.CyclopsModule;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ValidateJobConfigLambdaApplication {

  public static void main(String[] args) {
    SpringApplication.run(ValidateJobConfigLambdaApplication.class, args);
  }

  @Bean
  public AmazonS3 getAmazonS3Client() {
    return AmazonS3ClientBuilder.defaultClient();
  }

  @Bean
  public ObjectMapper objectMapper() {
    return new ObjectMapper().registerModule(new CyclopsModule());
  }
}
