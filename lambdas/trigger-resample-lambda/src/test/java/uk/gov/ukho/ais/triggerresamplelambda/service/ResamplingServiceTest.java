package uk.gov.ukho.ais.triggerresamplelambda.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.ukho.ais.emrjobrunner.EmrJobRunner;
import uk.gov.ukho.ais.emrjobrunner.model.AbstractJob;
import uk.gov.ukho.ais.triggerresamplelambda.configuration.ResampleLambdaConfiguration;
import uk.gov.ukho.ais.triggerresamplelambda.messaging.SqsMessageRetriever;
import uk.gov.ukho.ais.triggerresamplelambda.model.ResampleJob;
import uk.gov.ukho.ais.triggerresamplelambda.s3.BucketEmptier;

@RunWith(MockitoJUnitRunner.class)
public class ResamplingServiceTest {

  private final String jobLocation = "jobLocation";
  private final String inputLocation = "inputLocation";
  private final String outputLocation = "outputLocation";
  private final String jobClass = "jobClass";

  @Mock private SqsMessageRetriever mockSqsMessageRetriever;

  @Mock private EmrJobRunner mockEmrJobRunner;

  @Mock private Supplier<List<AbstractJob>> mockJobsSupplier;

  @Mock private BucketEmptier mockBucketEmptier;

  @Captor private ArgumentCaptor<List<AbstractJob>> argumentCaptor;

  @InjectMocks private ResamplingService resamplingService;

  @Rule public EnvironmentVariables environmentVariables = new EnvironmentVariables();

  @Before
  public void beforeEach() {
    environmentVariables.set("JOB_FULLY_QUALIFIED_CLASS_NAME", jobClass);
    environmentVariables.set("JOB_LOCATION", jobLocation);
    environmentVariables.set("INPUT_LOCATION", inputLocation);
    environmentVariables.set("OUTPUT_LOCATION", outputLocation);
  }

  @Test
  public void
      whenMessagesAreRetrievedThenEmrClusterCreatedWithTwoJobsForDifferentResampleParameters() {
    final String clusterId = "{\"clusterId\":\"123a\"}";

    final long distanceThreshold = 45L;
    final long timeThreshold = 6000L;

    when(mockSqsMessageRetriever.hasMessages()).thenReturn(true);
    when(mockEmrJobRunner.runEmrJobs(anyList())).thenReturn(clusterId);
    when(mockJobsSupplier.get())
        .thenReturn(
            Collections.singletonList(
                new ResampleJob(
                    distanceThreshold,
                    timeThreshold,
                    new ResampleLambdaConfiguration(),
                    "directory/")));

    final String result = resamplingService.resampleData();

    assertThat(result).isEqualTo(clusterId);

    verify(mockEmrJobRunner, times(1)).runEmrJobs(argumentCaptor.capture());
    verify(mockBucketEmptier, times(1)).emptyResampledBucket();

    assertThat(argumentCaptor.getValue())
        .extracting(AbstractJob::getJobSpecificParameters)
        .containsExactlyInAnyOrder(
            Arrays.asList(
                "--class",
                jobClass,
                jobLocation,
                "-i",
                inputLocation,
                "-o",
                outputLocation + "/directory/",
                "-d",
                String.valueOf(distanceThreshold),
                "-t",
                String.valueOf(timeThreshold)));
  }

  @Test
  public void whenNoMessagesRetrievedThenNoEmrJobsAreRun() {
    when(mockSqsMessageRetriever.hasMessages()).thenReturn(false);

    final String result = resamplingService.resampleData();

    assertThat(result).isEqualTo("");

    verify(mockEmrJobRunner, never()).runEmrJobs(anyList());
    verify(mockBucketEmptier, never()).emptyResampledBucket();
  }
}
