package uk.gov.ukho.ais.validatenewjobconfiglambda;

import org.springframework.cloud.function.adapter.aws.SpringBootRequestHandler;
import uk.gov.ukho.ais.validatenewjobconfiglambda.model.ValidationRequest;
import uk.gov.ukho.ais.validatenewjobconfiglambda.model.ValidationResult;

public class ValidateJobConfigLambdaHandler
    extends SpringBootRequestHandler<ValidationRequest, ValidationResult> {}
