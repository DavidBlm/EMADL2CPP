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
        List<AdaNetComponent> components = getAdaNetComponentsWithOneElement();
        CandidateBuilder candidateBuilder = new CandidateBuilder();
        ArchitectureSymbol candidate = candidateBuilder.createCandidate(components);
        assertNotNull(candidate);
    }

    private static List<AdaNetComponent> getAdaNetComponentsWithOneElement() {
        AdaNetComponent component = new AdaNetComponent(1);
        List<AdaNetComponent> components = new ArrayList<>();
        components.add(component);
        return components;
    }

    public void testCreateCandidateReturnsArchitectureWithParallelBlock() {
        List<AdaNetComponent> components = getAdaNetComponentsWithOneElement();
        CandidateBuilder candidateBuilder = new CandidateBuilder();

        ArchitectureSymbol candidate = candidateBuilder.createCandidate(components);

        ArchitectureElementSymbol parallelBlock = getParallelBlock(candidate);
        assertTrue(parallelBlock instanceof ParallelCompositeElementSymbol);
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

    public void testCreateCandidateReturnsArchitectureWithParallelBlockWithOneComponent() {
        List<AdaNetComponent> components = getAdaNetComponentsWithOneElement();
        CandidateBuilder candidateBuilder = new CandidateBuilder();

        ArchitectureSymbol candidate = candidateBuilder.createCandidate(components);

        ParallelCompositeElementSymbol parallelBlock = (ParallelCompositeElementSymbol) getParallelBlock(candidate);
        List<ArchitectureElementSymbol> parallelElements = parallelBlock.getElements();
        assertEquals(1, parallelElements.size());
    }
}