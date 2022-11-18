package de.monticore.mlpipelines.automl.trainalgorithms.adanet;

import de.monticore.lang.monticar.cnnarch._ast.*;
import de.monticore.lang.monticar.cnnarch._symboltable.ArchitectureElementSymbol;
import de.monticore.lang.monticar.cnnarch._symboltable.ArchitectureSymbol;
import de.monticore.mlpipelines.ModelLoader;
import de.monticore.mlpipelines.automl.trainalgorithms.adanet.models.AdaNetCandidate;
import de.monticore.mlpipelines.automl.trainalgorithms.adanet.models.AdaNetComponent;
import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;


@RunWith(MockitoJUnitRunner.class)
public class CandidateBuilderTest extends TestCase {
    private final CandidateBuilder candidateBuilder = new CandidateBuilder();

    public void setUp() throws Exception {
        super.setUp();
    }

    @Test
    public void testBuild() {
        ArchitectureSymbol refArch = ModelLoader.load("src/test/resources/models/adanet/", "adaNetBase");
        AdaNetCandidate candidate = new AdaNetCandidate(new AdaNetComponent(3), new ArrayList<>());
        ArchitectureElementSymbol newArch = candidateBuilder.build(candidate);

//        ASTStream layers = getAdanetLayers(newArch);
        assertNotNull(newArch);
    }

    private static ASTStream getAdanetLayers(ASTArchitecture newArch) {
        ASTStreamInstruction networkInstruction = (ASTStreamInstruction) newArch.getInstructions(0)
                .getNetworkInstruction();
        List<ASTArchitectureElement> elementsList = networkInstruction.getBody().getElementsList();
        ASTParallelBlock parallelBlock = (ASTParallelBlock) elementsList.get(1);
        ASTStream layers = parallelBlock.getGroups(0);
        return layers;
    }
}