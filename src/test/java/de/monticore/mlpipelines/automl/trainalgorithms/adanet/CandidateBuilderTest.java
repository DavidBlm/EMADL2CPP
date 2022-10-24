package de.monticore.mlpipelines.automl.trainalgorithms.adanet;

import de.monticore.lang.monticar.cnnarch._symboltable.ArchitectureSymbol;
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
}