package uk.gov.ukho.ais.validatenewjobconfiglambda.validation;

import static org.mockito.Mockito.when;

import cyclops.data.NonEmptyList;
import java.util.Arrays;
import java.util.Collections;
import org.assertj.core.api.SoftAssertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.ukho.ais.lambda.heatmap.job.model.JobConfig;
import uk.gov.ukho.ais.lambda.heatmap.job.model.validation.ValidationFailure;
import uk.gov.ukho.ais.lambda.heatmap.job.model.validation.ValidationResult;
import uk.gov.ukho.ais.validatenewjobconfiglambda.repository.FilterSqlFileRepository;

@RunWith(MockitoJUnitRunner.class)
public class ValidationResultFactoryTest {

  @Mock private FilterSqlFileRepository mockFilterSqlFileRepository;

  @InjectMocks private ValidationResultFactory validationResultFactory;

  private final String filterSqlFileName = "filter.sql";
  private final String filterSqlS3Uri = "s3://test-bucket/" + filterSqlFileName;
  private final NonEmptyList<ValidationFailure> validationFailures =
      NonEmptyList.of(ValidationFailure.FILTER_SQL_FILE_DOES_NOT_EXIST);

  @Test
  public void whenCreatingValidResultThenJobConfigIsPresent() {
    SoftAssertions.assertSoftly(
        softly -> {
          final JobConfig jobConfig = new JobConfig("output", 2019, 9, filterSqlFileName);

          when(mockFilterSqlFileRepository.getS3Uri(filterSqlFileName)).thenReturn(filterSqlS3Uri);

          final ValidationResult result = validationResultFactory.valid(jobConfig);

          softly.assertThat(result.getData().isPresent()).isTrue();
          softly
              .assertThat(result.getData().orElse(null))
              .extracting("output", "year", "month")
              .isEqualTo(Arrays.asList("output", 2019, 9));
        });
  }

  @Test
  public void whenCreatingValidResultThenFilterSqlFileIsFullS3Uri() {
    SoftAssertions.assertSoftly(
        softly -> {
          final JobConfig jobConfig = new JobConfig("output", 2019, 9, filterSqlFileName);

          when(mockFilterSqlFileRepository.getS3Uri(filterSqlFileName)).thenReturn(filterSqlS3Uri);

          final ValidationResult result = validationResultFactory.valid(jobConfig);

          softly
              .assertThat(result.getData().orElse(null))
              .extracting("filterSqlFile")
              .isEqualTo(Collections.singletonList(filterSqlS3Uri));
        });
  }

  @Test
  public void whenCreatingValidThenErrorIsEmpty() {
    SoftAssertions.assertSoftly(
        softly -> {
          final JobConfig jobConfig = new JobConfig("output", 2019, 9, filterSqlFileName);

          when(mockFilterSqlFileRepository.getS3Uri(filterSqlFileName)).thenReturn(filterSqlS3Uri);

          final ValidationResult result = validationResultFactory.valid(jobConfig);

          softly.assertThat(result.getError()).isEmpty();
        });
  }

  @Test
  public void whenCreatingInvalidThenHasNoData() {
    SoftAssertions.assertSoftly(
        softly -> {
          final ValidationResult result = validationResultFactory.invalid(validationFailures);

          softly.assertThat(result.getData().isPresent()).isFalse();
        });
  }

  @Test
  public void whenCreatingInvalidThenResultHasSuppliedValidationFailure() {
    SoftAssertions.assertSoftly(
        softly -> {
          final ValidationResult result = validationResultFactory.invalid(validationFailures);

          softly
              .assertThat(result.getError())
              .containsExactly(ValidationFailure.FILTER_SQL_FILE_DOES_NOT_EXIST);
        });
  }
}
