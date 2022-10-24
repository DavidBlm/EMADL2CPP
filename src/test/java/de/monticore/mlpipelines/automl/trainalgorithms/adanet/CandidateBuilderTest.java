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
        AdaNetComponent component = new AdaNetComponent(1);
        List<AdaNetComponent> components = new ArrayList<>();
        components.add(component);
        CandidateBuilder candidateBuilder = new CandidateBuilder();
        ArchitectureSymbol candidate = candidateBuilder.createCandidate(components);
        assertNotNull(candidate);
    }

    public void testCreateCandidateReturnsArchitectureWithParallelBlock() {
        AdaNetComponent component = new AdaNetComponent(1);
        List<AdaNetComponent> components = new ArrayList<>();
        components.add(component);
        CandidateBuilder candidateBuilder = new CandidateBuilder();

        ArchitectureSymbol candidate = candidateBuilder.createCandidate(components);

        NetworkInstructionSymbol networkInstruction = candidate.getNetworkInstructions().get(0);
        List<ArchitectureElementSymbol> elements = networkInstruction.getBody().getElements();
        ArchitectureElementSymbol parallelBlock = elements.get(1);
        assertTrue(parallelBlock instanceof ParallelCompositeElementSymbol);
    }
}