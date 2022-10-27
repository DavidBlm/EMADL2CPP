package de.monticore.mlpipelines.automl.trainalgorithms.adanet;

import de.monticore.lang.monticar.cnnarch._symboltable.ArchitectureElementSymbol;
import de.monticore.lang.monticar.cnnarch._symboltable.ArchitectureSymbol;
import de.monticore.lang.monticar.cnnarch._symboltable.NetworkInstructionSymbol;
import de.monticore.lang.monticar.cnnarch._symboltable.ParallelCompositeElementSymbol;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;

public class CandidateBuilderTest extends TestCase {

    public void testConstructor() {
        CandidateBuilder candidateBuilder = new CandidateBuilder();
        assertNotNull(candidateBuilder);
    }

    public void testCreateCandidate() {
        AdaNetCandidate candidate = getAdaNetCandidate();
        CandidateBuilder candidateBuilder = new CandidateBuilder();
        ArchitectureSymbol architecture = candidateBuilder.candidateToArchitectureSymbol(candidate);
        assertNotNull(architecture);
    }

    public void testCreateCandidateReturnsArchitectureWithParallelBlock() {
        AdaNetCandidate candidate = getAdaNetCandidate();
        CandidateBuilder candidateBuilder = new CandidateBuilder();

        ArchitectureSymbol architecture = candidateBuilder.candidateToArchitectureSymbol(candidate);
        ArchitectureElementSymbol parallelBlock = getParallelBlock(architecture);
        assertTrue(parallelBlock instanceof ParallelCompositeElementSymbol);
    }

    public void testCreateCandidateReturnsArchitectureWithParallelBlockWithOneComponent() {
        AdaNetCandidate candidate = getAdaNetCandidate();
        CandidateBuilder candidateBuilder = new CandidateBuilder();

        ArchitectureSymbol architecture = candidateBuilder.candidateToArchitectureSymbol(candidate);

        ParallelCompositeElementSymbol parallelBlock = (ParallelCompositeElementSymbol) getParallelBlock(architecture);
        List<ArchitectureElementSymbol> parallelElements = parallelBlock.getElements();
        assertEquals(1, parallelElements.size());
    }


    private static List<AdaNetComponent> getAdaNetComponentsWithOneElement() {
        AdaNetComponent component = new AdaNetComponent(1);
        List<AdaNetComponent> components = new ArrayList<>();
        components.add(component);
        return components;
    }

    private static AdaNetCandidate getAdaNetCandidate() {
        List<AdaNetComponent> previousComponents = new ArrayList<>();
        previousComponents.add(new AdaNetComponent(1));
        AdaNetComponent component = new AdaNetComponent(2);
        AdaNetCandidate candidate = new AdaNetCandidate(component, previousComponents);
        return candidate;
    }

    private static ArchitectureElementSymbol getParallelBlock(ArchitectureSymbol candidate) {
        List<ArchitectureElementSymbol> elements = getArchitectureElementSymbols(candidate);
        ArchitectureElementSymbol parallelBlock = elements.get(1);
        return parallelBlock;
    }

    private static List<ArchitectureElementSymbol> getArchitectureElementSymbols(ArchitectureSymbol candidate) {
        NetworkInstructionSymbol networkInstruction = candidate.getNetworkInstructions().get(0);
        List<ArchitectureElementSymbol> elements = networkInstruction.getBody().getElements();
        return elements;
    }
}