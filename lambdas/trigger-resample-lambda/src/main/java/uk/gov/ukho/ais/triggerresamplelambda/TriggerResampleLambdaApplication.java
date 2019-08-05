package uk.gov.ukho.ais.triggerresamplelambda;

import java.util.List;
import java.util.function.Function;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.function.context.FunctionalSpringApplication;
import org.springframework.context.annotation.Bean;
import uk.gov.ukho.ais.emrjobrunner.EmrJobRunner;
import uk.gov.ukho.ais.emrjobrunner.model.AbstractJob;
import uk.gov.ukho.ais.triggerresamplelambda.configuration.ResampleLambdaConfiguration;
import uk.gov.ukho.ais.triggerresamplelambda.function.TriggerResamplingFunction;
import uk.gov.ukho.ais.triggerresamplelambda.model.ResampleJob;

@SpringBootApplication
public class TriggerResampleLambdaApplication {

  private static final long DISTANCE_INTERPOLATION_THRESHOLD_30KM = 30L * 1000;
  private static final long DISTANCE_INTERPOLATION_THRESHOLD_100KM = 100L * 1000;
  private static final long ONE_HOUR_IN_MILLISECONDS = 60L * 60 * 1000;
  private static final long TIME_INTERPOLATION_THRESHOLD_6HR = 6L * ONE_HOUR_IN_MILLISECONDS;
  private static final long TIME_INTERPOLATION_THRESHOLD_18HR = 18L * ONE_HOUR_IN_MILLISECONDS;

  public static void main(String[] args) {
    FunctionalSpringApplication.run(TriggerResampleLambdaApplication.class, args);
  }

  @Bean
  public AbstractJob resampleJob30Km6HrJob(
      final ResampleLambdaConfiguration resampleLambdaConfiguration) {
    return new ResampleJob(
        DISTANCE_INTERPOLATION_THRESHOLD_30KM,
        TIME_INTERPOLATION_THRESHOLD_6HR,
        resampleLambdaConfiguration,
        "30km_6hr/");
  }

  @Bean
  public AbstractJob resampleJob100Km18HrJob(
      final ResampleLambdaConfiguration resampleLambdaConfiguration) {
    return new ResampleJob(
        DISTANCE_INTERPOLATION_THRESHOLD_100KM,
        TIME_INTERPOLATION_THRESHOLD_18HR,
        resampleLambdaConfiguration,
        "100km_18hr/");
  }

  @Bean
  public EmrJobRunner emrJobRunner(final ResampleLambdaConfiguration resampleLambdaConfiguration) {
    return new EmrJobRunner(resampleLambdaConfiguration);
  }

  @Bean
  public Function<String, String> lambdaFunction(
      final EmrJobRunner emrJobRunner, final List<AbstractJob> jobList) {
    return new TriggerResamplingFunction(emrJobRunner, jobList);
  }
}
