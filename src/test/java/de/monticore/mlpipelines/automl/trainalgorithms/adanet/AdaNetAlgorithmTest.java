package de.monticore.mlpipelines.automl.trainalgorithms.adanet;

import de.monticore.lang.monticar.cnnarch._symboltable.ArchitectureSymbol;
import de.monticore.mlpipelines.Pipeline;
import junit.framework.TestCase;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AdaNetAlgorithmTest extends TestCase {
    String modelPath = "src/test/resources/models/adanet/AdaNetBase.emadl";

    @Test
    public void testConstructor() {
        AdaNetAlgorithm adaNet = new AdaNetAlgorithm();
        assertNotNull(adaNet);
    }

    @Ignore
    public void testGetBestCandidateResult() {
        ArchitectureSymbol arch = mock(ArchitectureSymbol.class);
        AdaNetAlgorithm adaNet = new AdaNetAlgorithm();
        Pipeline pipeline = mock(Pipeline.class);
        adaNet.setTrainPipeline(pipeline);
        adaNet.execute(arch);
        assertNotNull(adaNet.getBestCandidateResult());
    }

    @Test
    public void testExecuteThrowsExceptionWhenTrainPipelineNotSet() {
        ArchitectureSymbol arch = mock(ArchitectureSymbol.class);
        AdaNetAlgorithm adanet = new AdaNetAlgorithm();
        boolean exceptionThrown = false;
        try {
            adanet.execute(arch);
            fail("Expected exception");
        } catch (IllegalStateException e) {
            exceptionThrown = true;
            assertEquals("Train pipeline not set", e.getMessage());
        }

        assertTrue(exceptionThrown);
    }

    @Ignore
    public void testExecuteStopsAtFirstIteration() {
        ArchitectureSymbol arch = mock(ArchitectureSymbol.class);
        AdaNetAlgorithm adanet = new AdaNetAlgorithm();
        Pipeline pipeline = mock(Pipeline.class);
        when(pipeline.getTrainedAccuracy()).thenReturn(0.0f);
        adanet.setTrainPipeline(pipeline);
        adanet.execute(arch);

        CandidateEvaluationResult evaluationResult = adanet.getBestCandidateResult();
        List<AdaNetComponent> previousComponents = evaluationResult.getCandidate().getPreviousComponents();
        assertNotNull(evaluationResult);
        assertTrue(previousComponents.isEmpty());
    }

    @Ignore
    public void testExecuteBestCandidateHasTwoCandidates() {
        ArchitectureSymbol arch = mock(ArchitectureSymbol.class);
        AdaNetAlgorithm adanet = new AdaNetAlgorithm();
        Pipeline pipeline = mock(Pipeline.class);
        when(pipeline.getTrainedAccuracy()).thenReturn(0.0f, 0.1f, 0.2f);
        adanet.setTrainPipeline(pipeline);
        adanet.execute(arch);

        CandidateEvaluationResult evaluationResult = adanet.getBestCandidateResult();
        List<AdaNetComponent> previousComponents = evaluationResult.getCandidate().getPreviousComponents();
        assertNotNull(evaluationResult);
        assertEquals(1, previousComponents.size());
    }

    @Ignore
    public void testExecuteBestCandidateHasScore() {
        ArchitectureSymbol arch = mock(ArchitectureSymbol.class);
        AdaNetAlgorithm adanet = new AdaNetAlgorithm();
        Pipeline pipeline = mock(Pipeline.class);
        when(pipeline.getTrainedAccuracy()).thenReturn(0.0f, 0.1f, 0.2f);
        adanet.setTrainPipeline(pipeline);
        adanet.execute(arch);

        CandidateEvaluationResult evaluationResult = adanet.getBestCandidateResult();
        List<AdaNetComponent> previousComponents = evaluationResult.getCandidate().getPreviousComponents();
        assertEquals(0.2f, evaluationResult.getScore());

    }
}