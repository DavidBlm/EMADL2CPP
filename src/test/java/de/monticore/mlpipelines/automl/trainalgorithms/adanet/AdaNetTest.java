package de.monticore.mlpipelines.automl.trainalgorithms.adanet;

import de.monticore.lang.monticar.cnnarch._symboltable.ArchitectureSymbol;
import de.monticore.mlpipelines.ModelLoader;
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
    @Test
    public void testConstructor() {
        AdaNet adaNet = new AdaNet();
        assertNotNull(adaNet);
    }

    @Test
    public void testGetBestCandidateResult() {
        AdaNet adaNet = new AdaNet();
        Pipeline pipeline = mock(Pipeline.class);
        adaNet.setTrainPipeline(pipeline);
        ArchitectureSymbol architecture = ModelLoader.loadAdaNet();
        adaNet.execute();
        assertNotNull(adaNet.getBestCandidateResult());
    }

    @Test
    public void testExecuteThrowsExceptionWhenTrainPipelineNotSet() {
        AdaNet adanet = new AdaNet();
        ArchitectureSymbol architectureSymbol = ModelLoader.loadAdaNet();
        boolean exceptionThrown = false;
        try {
            adanet.execute(architectureSymbol);
            fail("Expected exception");
        } catch (IllegalStateException e) {
            exceptionThrown = true;
            assertEquals("Train pipeline not set", e.getMessage());
        }

        assertTrue(exceptionThrown);
    }

    @Test
    public void testExecuteStopsAtFirstIteration() {
        AdaNet adanet = new AdaNet();
        ArchitectureSymbol architectureSymbol = ModelLoader.loadAdaNet();
        Pipeline pipeline = mock(Pipeline.class);
        when(pipeline.getTrainedAccuracy()).thenReturn(0.0f);
        adanet.setTrainPipeline(pipeline);
        adanet.execute(architectureSymbol);

        CandidateEvaluationResult evaluationResult = adanet.getBestCandidateResult();
        List<AdaNetComponent> previousComponents = evaluationResult.getCandidate().getPreviousComponents();
        assertNotNull(evaluationResult);
        assertTrue(previousComponents.isEmpty());
    }

    @Test
    public void testExecuteBestCandidateHasTwoCandidates() {
        AdaNet adanet = new AdaNet();
        ArchitectureSymbol architectureSymbol = ModelLoader.loadAdaNet();
        Pipeline pipeline = mock(Pipeline.class);
        when(pipeline.getTrainedAccuracy()).thenReturn(0.0f, 0.1f, 0.2f);
        adanet.setTrainPipeline(pipeline);
        adanet.execute(architectureSymbol);

        CandidateEvaluationResult evaluationResult = adanet.getBestCandidateResult();
        List<AdaNetComponent> previousComponents = evaluationResult.getCandidate().getPreviousComponents();
        assertNotNull(evaluationResult);
        assertEquals(1, previousComponents.size());
    }

    @Test
    public void testExecuteBestCandidateHasScore() {
        AdaNet adanet = new AdaNet();
        ArchitectureSymbol architectureSymbol = ModelLoader.loadAdaNet();
        Pipeline pipeline = mock(Pipeline.class);
        when(pipeline.getTrainedAccuracy()).thenReturn(0.0f, 0.1f, 0.2f);
        adanet.setTrainPipeline(pipeline);
        adanet.execute(architectureSymbol);

        CandidateEvaluationResult evaluationResult = adanet.getBestCandidateResult();
        List<AdaNetComponent> previousComponents = evaluationResult.getCandidate().getPreviousComponents();
        assertEquals(0.2f, evaluationResult.getScore());

    }
}