package de.monticore.mlpipelines.automl.trainalgorithms.adanet.builder;

import de.monticore.lang.monticar.cnnarch._symboltable.ArchitectureSymbol;
import de.monticore.mlpipelines.ModelLoader;
import de.monticore.mlpipelines.automl.trainalgorithms.adanet.models.AdaNetCandidate;
import de.monticore.mlpipelines.automl.trainalgorithms.adanet.models.AdaNetComponent;
import junit.framework.TestCase;

import java.util.ArrayList;

public class CandidateBuilderTest extends TestCase {

    public void testConstructor() {
        CandidateBuilder candidateBuilder = new CandidateBuilder();
        assertNotNull(candidateBuilder);
    }

    public void testBuild() {
        CandidateBuilder candidateBuilder = new CandidateBuilder();
        ArchitectureSymbol originalArchitecture = ModelLoader.loadAdaNetBase();
        AdaNetCandidate candidate = new AdaNetCandidate(new AdaNetComponent(3), new ArrayList<>());

        ArchitectureSymbol candidateArchitecture = candidateBuilder.build(candidate, originalArchitecture);
        assertNotNull(candidateBuilder);
    }
}