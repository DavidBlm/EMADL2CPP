package de.monticore.mlpipelines.automl.trainalgorithms.adanet;

import de.monticore.mlpipelines.Pipeline;
import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AdaNetTest extends TestCase {
    String modelPath = "src/test/resources/models/adanet/AdaNetBase.emadl";

    @Test
    public void testConstructor() {
        AdaNetAlgorithm adaNet = new AdaNetAlgorithm(modelPath);
        assertNotNull(adaNet);
    }

    @Test
    public void testGetBestCandidateResult() {
        AdaNetAlgorithm adaNet = new AdaNetAlgorithm(modelPath);
        Pipeline pipeline = mock(Pipeline.class);
        adaNet.setTrainPipeline(pipeline);
        adaNet.execute();
        assertNotNull(adaNet.getBestCandidateResult());
    }

    @Test
    public void testExecuteThrowsExceptionWhenTrainPipelineNotSet() {
        AdaNetAlgorithm adanet = new AdaNetAlgorithm(modelPath);
        boolean exceptionThrown = false;
        try {
            adanet.execute();
            fail("Expected exception");
        } catch (IllegalStateException e) {
            exceptionThrown = true;
            assertEquals("Train pipeline not set", e.getMessage());
        }

        assertTrue(exceptionThrown);
    }

    @Test
    public void testExecuteStopsAtFirstIteration() {
        AdaNetAlgorithm adanet = new AdaNetAlgorithm(modelPath);
        Pipeline pipeline = mock(Pipeline.class);
        when(pipeline.getTrainedAccuracy()).thenReturn(0.0f);
        adanet.setTrainPipeline(pipeline);
        adanet.execute();

        CandidateEvaluationResult evaluationResult = adanet.getBestCandidateResult();
        List<AdaNetComponent> previousComponents = evaluationResult.getCandidate().getPreviousComponents();
        assertNotNull(evaluationResult);
        assertTrue(previousComponents.isEmpty());
    }

    @Test
    public void testExecuteBestCandidateHasTwoCandidates() {
        AdaNetAlgorithm adanet = new AdaNetAlgorithm(modelPath);
        Pipeline pipeline = mock(Pipeline.class);
        when(pipeline.getTrainedAccuracy()).thenReturn(0.0f, 0.1f, 0.2f);
        adanet.setTrainPipeline(pipeline);
        adanet.execute();

        CandidateEvaluationResult evaluationResult = adanet.getBestCandidateResult();
        List<AdaNetComponent> previousComponents = evaluationResult.getCandidate().getPreviousComponents();
        assertNotNull(evaluationResult);
        assertEquals(1, previousComponents.size());
    }

    @Test
    public void testExecuteBestCandidateHasScore() {
        AdaNetAlgorithm adanet = new AdaNetAlgorithm(modelPath);
        Pipeline pipeline = mock(Pipeline.class);
        when(pipeline.getTrainedAccuracy()).thenReturn(0.0f, 0.1f, 0.2f);
        adanet.setTrainPipeline(pipeline);
        adanet.execute();

        CandidateEvaluationResult evaluationResult = adanet.getBestCandidateResult();
        List<AdaNetComponent> previousComponents = evaluationResult.getCandidate().getPreviousComponents();
        assertEquals(0.2f, evaluationResult.getScore());

    }
}