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
    public void testSetPipeline() {
        AdaNet adaNet = new AdaNet();
        Pipeline pipeline = mock(Pipeline.class);
        adaNet.setPipeline(pipeline);
        assertNotNull(adaNet.getPipeline());
    }

    @Test
    public void testGetBestCandidateResult() {
        AdaNet adaNet = new AdaNet();
        Pipeline pipeline = mock(Pipeline.class);
        adaNet.setPipeline(pipeline);
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

    public void testExecuteStopsAtFirstIteration() {
        AdaNet adanet = new AdaNet();
        ArchitectureSymbol architectureSymbol = ModelLoader.loadAdaNet();
        Pipeline pipeline = mock(Pipeline.class);
        CandidateEvaluationResult result = new CandidateEvaluationResult(null, 0);
        when(pipeline.getTrainedAccuracy()).thenReturn(0.0f);
        adanet.setTrainPipeline(pipeline);
        adanet.execute(architectureSymbol);

        CandidateEvaluationResult evaluationResult = adanet.getBestCandidateResult();
        List<AdaNetComponent> previousComponents = evaluationResult.getCandidate().getPreviousComponents();
        assertNotNull(evaluationResult);
        assertTrue(previousComponents.isEmpty());
    }
}