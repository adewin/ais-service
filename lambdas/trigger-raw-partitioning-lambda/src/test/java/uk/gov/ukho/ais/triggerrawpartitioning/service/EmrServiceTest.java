package uk.gov.ukho.ais.triggerrawpartitioning.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

import com.amazonaws.services.lambda.runtime.events.S3Event;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.ukho.ais.emrjobrunner.EmrJobRunner;
import uk.gov.ukho.ais.emrjobrunner.model.AbstractJob;
import uk.gov.ukho.ais.s3eventhandling.model.S3Object;
import uk.gov.ukho.ais.s3eventhandling.model.S3ObjectEvent;
import uk.gov.ukho.ais.s3testutil.S3EventUtil;
import uk.gov.ukho.ais.triggerrawpartitioning.model.PartitionJob;

@RunWith(MockitoJUnitRunner.class)
public class EmrServiceTest {

  @Mock private EmrJobRunner mockEmrJobRunner;

  @InjectMocks private EmrService emrService;

  @Captor private ArgumentCaptor<List<AbstractJob>> jobsRanOnClusterArgCaptor;

  private final String bucket = "test-bucket";
  private final String object = "test-data.txt";
  private final String createdEvent = S3ObjectEvent.CREATED.getEvents().get(0);
  private final String removedEvent = S3ObjectEvent.REMOVED.getEvents().get(0);

  @After
  public void after() {
    reset(mockEmrJobRunner, mockEmrJobRunner);
  }

  @Test
  public void whenNoEventsOccurThenNoSparkJobCreated() {
    final S3Event s3Event = new S3Event(Collections.emptyList());

    final String result = emrService.triggerPartitionJobsForS3Event(s3Event);

    assertThat(result).isEqualTo(null);

    verify(mockEmrJobRunner, never()).runEmrJobs(anyList());
  }

  @Test
  public void whenCreatedEventOccursThenSparkJobWithOneStepCreated() {
    final S3Event s3Event =
        new S3Event(
            Collections.singletonList(S3EventUtil.createRecordFor(bucket, object, createdEvent)));

    final String clusterValue = "{\"cluster\": \"xyz\"}";

    when(mockEmrJobRunner.runEmrJobs(anyList())).thenReturn(clusterValue);

    final String result = emrService.triggerPartitionJobsForS3Event(s3Event);

    assertThat(result).isEqualTo(clusterValue);

    verify(mockEmrJobRunner, times(1)).runEmrJobs(jobsRanOnClusterArgCaptor.capture());

    List<AbstractJob> jobsRanOnCluster = jobsRanOnClusterArgCaptor.getValue();

    assertThat(jobsRanOnCluster)
        .usingFieldByFieldElementComparator()
        .hasSize(1)
        .containsExactly(
            new PartitionJob(new S3Object(bucket, object, S3ObjectEvent.CREATED).toString()));
  }

  @Test
  public void whenTwoCreationEventsOccurThenSparkJobTriggeredWithTwoStepsCreated() {
    final S3Event s3Event =
        new S3Event(
            Arrays.asList(
                S3EventUtil.createRecordFor(bucket, object, createdEvent),
                S3EventUtil.createRecordFor(bucket, "another-file.txt", createdEvent)));

    final String clusterValue = "{\"cluster\": \"xyz\"}";

    when(mockEmrJobRunner.runEmrJobs(anyList())).thenReturn(clusterValue);

    final String result = emrService.triggerPartitionJobsForS3Event(s3Event);

    assertThat(result).isEqualTo(clusterValue);

    verify(mockEmrJobRunner, times(1)).runEmrJobs(jobsRanOnClusterArgCaptor.capture());

    List<AbstractJob> jobsRanOnCluster = jobsRanOnClusterArgCaptor.getValue();

    assertThat(jobsRanOnCluster)
        .usingFieldByFieldElementComparator()
        .hasSize(2)
        .containsExactlyInAnyOrder(
            new PartitionJob(new S3Object(bucket, object, S3ObjectEvent.CREATED).toString()),
            new PartitionJob(
                new S3Object(bucket, "another-file.txt", S3ObjectEvent.CREATED).toString()));
  }

  @Test
  public void whenChangesOccurThatAreNotCreationEventsThenNoSparkJobRun() {
    final S3Event s3Event =
        new S3Event(
            Collections.singletonList(S3EventUtil.createRecordFor(bucket, object, removedEvent)));

    final String result = emrService.triggerPartitionJobsForS3Event(s3Event);

    assertThat(result).isEqualTo(null);

    verify(mockEmrJobRunner, never()).runEmrJobs(anyList());
  }

  @Test
  public void
      whenOneCreationEventOccursAtSameTimeAsADeletionEventThenOnlyStepCreatedForCreationEvent() {
    final S3Event s3Event =
        new S3Event(
            Arrays.asList(
                S3EventUtil.createRecordFor(bucket, object, createdEvent),
                S3EventUtil.createRecordFor(bucket, "another-file.txt", removedEvent)));

    final String clusterValue = "{\"cluster\": \"xyz\"}";

    when(mockEmrJobRunner.runEmrJobs(anyList())).thenReturn(clusterValue);

    final String result = emrService.triggerPartitionJobsForS3Event(s3Event);

    assertThat(result).isEqualTo(clusterValue);

    verify(mockEmrJobRunner, times(1)).runEmrJobs(jobsRanOnClusterArgCaptor.capture());

    List<AbstractJob> jobsRanOnCluster = jobsRanOnClusterArgCaptor.getValue();

    assertThat(jobsRanOnCluster)
        .usingFieldByFieldElementComparator()
        .hasSize(1)
        .containsExactlyInAnyOrder(
            new PartitionJob(new S3Object(bucket, object, S3ObjectEvent.CREATED).toString()));
  }
}
