package uk.gov.ukho.ais.validatenewjobconfiglambda;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.oath.cyclops.jackson.CyclopsModule;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

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
  public Jackson2ObjectMapperBuilder objectMapper() {
    return new Jackson2ObjectMapperBuilder()
        .serializationInclusion(JsonInclude.Include.NON_NULL)
        .modules(new CyclopsModule());
  }
}
