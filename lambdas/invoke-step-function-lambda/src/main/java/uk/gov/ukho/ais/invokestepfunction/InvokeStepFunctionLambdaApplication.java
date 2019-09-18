package uk.gov.ukho.ais.invokestepfunction;

import com.amazonaws.services.stepfunctions.AWSStepFunctions;
import com.amazonaws.services.stepfunctions.AWSStepFunctionsClientBuilder;
import com.oath.cyclops.jackson.CyclopsModule;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

@SpringBootApplication
public class InvokeStepFunctionLambdaApplication {

  public static void main(String[] args) {
    SpringApplication.run(InvokeStepFunctionLambdaApplication.class, args);
  }

  @Bean
  public AWSStepFunctions awsStepFunctions() {
    return AWSStepFunctionsClientBuilder.defaultClient();
  }

  @Bean
  public Jackson2ObjectMapperBuilder objectMapper() {
    return new Jackson2ObjectMapperBuilder().modules(new CyclopsModule());
  }
}
