package de.monticore.mlpipelines.automl.trainalgorithms.adanet;

import de.monticore.lang.monticar.cnnarch._symboltable.ArchitectureElementSymbol;
import de.monticore.lang.monticar.cnnarch._symboltable.ArchitectureSymbol;
import de.monticore.lang.monticar.cnnarch._symboltable.StreamInstructionSymbol;
import de.monticore.mlpipelines.ModelLoader;
import de.monticore.mlpipelines.automl.trainalgorithms.adanet.custom.models.ParallelCompositeElementSymbolCustom;
import de.monticore.mlpipelines.automl.trainalgorithms.adanet.models.AdaNetComponent;
import de.monticore.mlpipelines.automl.trainalgorithms.adanet.models.CandidateEvaluationResult;
import de.monticore.mlpipelines.pipelines.Pipeline;
import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;

import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public class AdaNetAlgorithmTest extends TestCase {
    String modelPath = "src/test/resources/models/adanet/AdaNetCustom.emadl";

    @Test
    public void testConstructor() {
        AdaNetAlgorithm adaNet = new AdaNetAlgorithm();
        assertNotNull(adaNet);
    }

    @Test
    public void testGetBestCandidateResult() {
        ArchitectureSymbol arch = ModelLoader.loadAdaNetBase();
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

    @Test
    public void testExecuteStopsAtFirstIteration() {
        ArchitectureSymbol arch = ModelLoader.loadAdaNetBase();
        AdaNetAlgorithm adanet = new AdaNetAlgorithm();
        Pipeline pipeline = mock(Pipeline.class);
//        when(pipeline.getTrainedAccuracy()).thenReturn(0.0f);
        adanet.setTrainPipeline(pipeline);
        adanet.execute(arch);

        CandidateEvaluationResult evaluationResult = adanet.getBestCandidateResult();
        List<AdaNetComponent> previousComponents = evaluationResult.getCandidate().getPreviousComponents();
        assertNotNull(evaluationResult);
        assertTrue(previousComponents.isEmpty());
    }

    @Test
    public void testExecuteBestCandidateHasTwoCandidates() {
        ArchitectureSymbol arch = ModelLoader.loadAdaNetBase();
        AdaNetAlgorithm adanet = new AdaNetAlgorithm();
        Pipeline pipeline = mock(Pipeline.class);
//        when(pipeline.getTrainedAccuracy()).thenReturn(0.0f, 0.1f, 0.2f);
        adanet.setTrainPipeline(pipeline);
        adanet.execute(arch);

        CandidateEvaluationResult evaluationResult = adanet.getBestCandidateResult();
        List<AdaNetComponent> previousComponents = evaluationResult.getCandidate().getPreviousComponents();
        assertNotNull(evaluationResult);
        assertEquals(1, previousComponents.size());
    }

    @Test
    public void testExecuteBestCandidateHasScore() {
        ArchitectureSymbol arch = ModelLoader.loadAdaNetBase();
        AdaNetAlgorithm adanet = new AdaNetAlgorithm();
        Pipeline pipeline = mock(Pipeline.class);
//        when(pipeline.getTrainedAccuracy()).thenReturn(0.0f, 0.1f, 0.2f);
        adanet.setTrainPipeline(pipeline);
        adanet.execute(arch);

        CandidateEvaluationResult evaluationResult = adanet.getBestCandidateResult();
        List<AdaNetComponent> previousComponents = evaluationResult.getCandidate().getPreviousComponents();
        assertEquals(0.2f, evaluationResult.getScore());
    }

    @Test
    public void testExecuteReplacesAdaNet() {
        ArchitectureSymbol arch = ModelLoader.loadAdaNetBase();
        AdaNetAlgorithm adanet = new AdaNetAlgorithm();
        Pipeline pipeline = mock(Pipeline.class);
//        when(pipeline.getTrainedAccuracy()).thenReturn(0.0f, 0.1f, 0.2f);
        adanet.setTrainPipeline(pipeline);
        adanet.execute(arch);

        StreamInstructionSymbol networkInstructionSymbol = (StreamInstructionSymbol) arch.getNetworkInstructions()
                .get(0);
        List<ArchitectureElementSymbol> elements = networkInstructionSymbol.getBody().getElements();
        assertEquals(ParallelCompositeElementSymbolCustom.class, elements.get(1).getClass());
    }
}