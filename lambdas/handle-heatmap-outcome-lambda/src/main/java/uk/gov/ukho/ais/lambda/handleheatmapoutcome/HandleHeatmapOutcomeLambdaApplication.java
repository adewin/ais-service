package uk.gov.ukho.ais.lambda.handleheatmapoutcome;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.oath.cyclops.jackson.CyclopsModule;
import java.time.Clock;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

@SpringBootApplication
public class HandleHeatmapOutcomeLambdaApplication {

  public static void main(final String[] args) {
    SpringApplication.run(HandleHeatmapOutcomeLambdaApplication.class, args);
  }

  @Bean
  public Jackson2ObjectMapperBuilder objectMapper() {
    return new Jackson2ObjectMapperBuilder()
        .serializationInclusion(JsonInclude.Include.NON_NULL)
        .modules(new CyclopsModule());
  }

  @Bean
  public AmazonS3 amazonS3() {
    return AmazonS3ClientBuilder.defaultClient();
  }

  @Bean
  public Clock clock() {
    return Clock.systemUTC();
  }
}
