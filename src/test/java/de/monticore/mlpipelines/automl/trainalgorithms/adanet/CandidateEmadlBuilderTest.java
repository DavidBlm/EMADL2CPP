package de.monticore.mlpipelines.automl.trainalgorithms.adanet;

import de.monticore.lang.monticar.cnnarch._symboltable.*;
import de.monticore.mlpipelines.automl.trainalgorithms.adanet.models.AdaNetCandidate;
import de.monticore.mlpipelines.automl.trainalgorithms.adanet.models.AdaNetComponent;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;

public class CandidateEmadlBuilderTest extends TestCase {
    String modelPath = "src/test/resources/models/adanet/AdaNetBase.emadl";
    String generatedModelPath = "src/test/resources/models/adanet/AdaNetBaseGenerated.emadl";

    public void testConstructor() {
        CandidateEmadlBuilder candidateBuilder = new CandidateEmadlBuilder(modelPath, generatedModelPath);
        assertNotNull(candidateBuilder);
    }

    public void testCreateCandidate() {
        AdaNetCandidate candidate = getAdaNetCandidate();
        CandidateEmadlBuilder candidateBuilder = new CandidateEmadlBuilder(modelPath, generatedModelPath);
        ArchitectureSymbol architecture = candidateBuilder.createArchitectureFromCandidate(candidate);
        assertNotNull(architecture);
    }

    public void testCreateCandidateHas7Layers() {
        AdaNetCandidate candidate = getAdaNetCandidate();
        CandidateEmadlBuilder candidateBuilder = new CandidateEmadlBuilder(modelPath, generatedModelPath);
        ArchitectureSymbol architecture = candidateBuilder.createArchitectureFromCandidate(candidate);
        List<ArchitectureElementSymbol> layers = getLayers(architecture);
        assertEquals(7, layers.size());
    }

    private static List<ArchitectureElementSymbol> getLayers(ArchitectureSymbol architecture) {
        NetworkInstructionSymbol networkInstruction = architecture.getNetworkInstructions().get(0);
        SerialCompositeElementSymbol serialCompositeElement = networkInstruction.getBody();
        List<ArchitectureElementSymbol> layers = serialCompositeElement.getElements();
        return layers;
    }

    public void testCreateCandidateHasClassificationLayer() {
        AdaNetCandidate candidate = getAdaNetCandidate();
        CandidateEmadlBuilder candidateBuilder = new CandidateEmadlBuilder(modelPath, generatedModelPath);
        ArchitectureSymbol architecture = candidateBuilder.createArchitectureFromCandidate(candidate);

        List<ArchitectureElementSymbol> layers = getLayers(architecture);
        LayerSymbol classificationLayer = (LayerSymbol) layers.get(4);
        List<ArgumentSymbol> arguments = classificationLayer.getArguments();
        assertEquals("FullyConnected", classificationLayer.getName());

        assertEquals(1, arguments.size());
        assertEquals("units", arguments.get(0).getName());
    }

    public void testCreateCandidateHasParallelLayer() {
        AdaNetCandidate candidate = getAdaNetCandidate();
        CandidateEmadlBuilder candidateBuilder = new CandidateEmadlBuilder(modelPath, generatedModelPath);
        ArchitectureSymbol architecture = candidateBuilder.createArchitectureFromCandidate(candidate);

        List<ArchitectureElementSymbol> layers = getLayers(architecture);
        LayerSymbol parallelLayer = (LayerSymbol) layers.get(1);
        List<ArgumentSymbol> arguments = parallelLayer.getArguments();
        assertEquals("FullyConnected", parallelLayer.getName());

        assertEquals(2, arguments.size());
        assertEquals("|", arguments.get(0).getName());
        assertEquals("units", arguments.get(1).getName());
    }

    public void testCreateCandidateHasConcatenateAfterParallelLayer() {
        AdaNetCandidate candidate = getAdaNetCandidate();
        CandidateEmadlBuilder candidateBuilder = new CandidateEmadlBuilder(modelPath, generatedModelPath);
        ArchitectureSymbol architecture = candidateBuilder.createArchitectureFromCandidate(candidate);

        List<ArchitectureElementSymbol> layers = getLayers(architecture);
        LayerSymbol concatenateLayer = (LayerSymbol) layers.get(2);
        assertEquals("Concatenate", concatenateLayer.getName());
    }

    public void testCreateCandidateHasNonParallelLayer() {
        AdaNetCandidate candidate = getAdaNetCandidate();
        CandidateEmadlBuilder candidateBuilder = new CandidateEmadlBuilder(modelPath, generatedModelPath);
        ArchitectureSymbol architecture = candidateBuilder.createArchitectureFromCandidate(candidate);

        List<ArchitectureElementSymbol> layers = getLayers(architecture);
        LayerSymbol nonParallelLayer = (LayerSymbol) layers.get(3);
        List<ArgumentSymbol> arguments = nonParallelLayer.getArguments();
        assertEquals("FullyConnected", nonParallelLayer.getName());

        assertEquals(1, arguments.size());
        assertEquals("units", arguments.get(0).getName());
    }

    private static AdaNetCandidate getAdaNetCandidate() {
        List<AdaNetComponent> previousComponents = new ArrayList<>();
        previousComponents.add(new AdaNetComponent(1));
        AdaNetComponent component = new AdaNetComponent(2);
        AdaNetCandidate candidate = new AdaNetCandidate(component, previousComponents);
        return candidate;
    }

    private static List<ArchitectureElementSymbol> getArchitectureElementSymbols(ArchitectureSymbol candidate) {
        NetworkInstructionSymbol networkInstruction = candidate.getNetworkInstructions().get(0);
        List<ArchitectureElementSymbol> elements = networkInstruction.getBody().getElements();
        return elements;
    }
}