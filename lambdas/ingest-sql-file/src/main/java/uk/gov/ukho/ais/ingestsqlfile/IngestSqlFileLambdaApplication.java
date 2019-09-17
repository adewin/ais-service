package uk.gov.ukho.ais.ingestsqlfile;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class IngestSqlFileLambdaApplication {
  public static void main(final String[] args) {
    SpringApplication.run(IngestSqlFileLambdaApplication.class, args);
  }

  @Bean
  public AmazonS3 amazonS3() {
    return AmazonS3ClientBuilder.defaultClient();
  }
}
