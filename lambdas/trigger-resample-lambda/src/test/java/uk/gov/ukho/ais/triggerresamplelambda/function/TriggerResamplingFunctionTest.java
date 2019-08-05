package uk.gov.ukho.ais.triggerresamplelambda.function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.ukho.ais.emrjobrunner.EmrJobRunner;
import uk.gov.ukho.ais.emrjobrunner.model.AbstractJob;

@RunWith(MockitoJUnitRunner.class)
public class TriggerResamplingFunctionTest {

  private final String jobLocation = "jobLocation";
  private final String inputLocation = "inputLocation";
  private final String outputLocation = "outputLocation";
  private final String jobClass = "jobClass";

  @Mock private EmrJobRunner mockEmrJobRunner;

  private final List<AbstractJob> jobs = Collections.emptyList();

  @Captor private ArgumentCaptor<List<AbstractJob>> argumentCaptor;

  private TriggerResamplingFunction triggerResamplingFunction;

  @Before
  public void beforeEach() {
    triggerResamplingFunction = new TriggerResamplingFunction(mockEmrJobRunner, jobs);
  }

  @Test
  public void
      whenMessagesAreRetrievedThenEmrClusterCreatedWithTwoJobsForDifferentResampleParameters() {
    final String clusterId = "{\"clusterId\":\"123a\"}";

    final long distanceThreshold = 45L;
    final long timeThreshold = 6000L;

    when(mockEmrJobRunner.runEmrJobs(jobs)).thenReturn(clusterId);

    final String result = triggerResamplingFunction.apply("");

    assertThat(result).isEqualTo(clusterId);

    verify(mockEmrJobRunner, times(1)).runEmrJobs(jobs);
  }
}
