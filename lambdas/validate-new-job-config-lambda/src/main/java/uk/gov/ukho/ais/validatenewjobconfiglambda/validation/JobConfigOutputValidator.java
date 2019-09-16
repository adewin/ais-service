package uk.gov.ukho.ais.validatenewjobconfiglambda.validation;

import com.amazonaws.services.s3.AmazonS3;
import cyclops.control.Either;
import cyclops.control.Option;
import cyclops.control.Try;
import cyclops.control.Validated;
import java.util.Objects;
import org.springframework.stereotype.Component;
import uk.gov.ukho.ais.lambda.heatmap.job.model.JobConfig;
import uk.gov.ukho.ais.lambda.heatmap.job.model.validation.ValidationFailure;

@Component
public class JobConfigOutputValidator implements Validator {

  private final AmazonS3 amazonS3;

  public JobConfigOutputValidator(AmazonS3 amazonS3) {
    this.amazonS3 = amazonS3;
  }

  @Override
  public Validated<ValidationFailure, JobConfig> validate(
      final Either<ValidationFailure, JobConfig> jobConfigOrFailure) {
    return Validator.isNotEmpty(jobConfigOrFailure, JobConfig::getOutput)
        .apply(ValidationFailure.JOB_CONFIG_DOES_NOT_HAVE_OUTPUT)
        .combine(Validator.RETURN_FIRST, outputS3BucketExists(jobConfigOrFailure));
  }

  private Validated<ValidationFailure, JobConfig> outputS3BucketExists(
      final Either<ValidationFailure, JobConfig> jobConfigOrFailure) {
    return jobConfigOrFailure
        .fold(l -> Option.<JobConfig>none(), Option::of)
        .map(JobConfig::getOutput)
        .filter(Objects::nonNull)
        .map(output -> Try.withCatch(() -> amazonS3.doesBucketExistV2(output)))
        .filter(existTry -> existTry.toEither().fold(exception -> true, value -> !value))
        .map(
            headBucketResult ->
                Validated.<ValidationFailure, JobConfig>invalid(
                    ValidationFailure.OUTPUT_S3_BUCKET_DOES_NOT_EXIST))
        .orElse(Validator.valid(jobConfigOrFailure));
  }
}
