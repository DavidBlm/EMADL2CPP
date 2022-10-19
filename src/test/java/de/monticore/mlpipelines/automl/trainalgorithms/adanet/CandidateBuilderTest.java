package de.monticore.mlpipelines.automl.trainalgorithms.adanet;

import de.monticore.lang.monticar.cnnarch._symboltable.ArchitectureSymbol;
import junit.framework.TestCase;

public class CandidateBuilderTest extends TestCase {

    public void testConstructor() {
        CandidateBuilder candidateBuilder = new CandidateBuilder();
        assertNotNull(candidateBuilder);
    }

    public void testCreateCandidate() {
        ArchitectureSymbol architecture = new ArchitectureSymbol();
        AdaNetComponent component = new AdaNetComponent(1);
        CandidateBuilder candidateBuilder = new CandidateBuilder();
        ArchitectureSymbol candidate = candidateBuilder.createCandidate(architecture, component);
        assertNotNull(candidate);
    }


}