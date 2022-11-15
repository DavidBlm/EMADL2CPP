package de.monticore.mlpipelines.automl.trainalgorithms.adanet;

import de.monticore.lang.monticar.cnnarch._ast.ASTArchitecture;
import de.monticore.lang.monticar.cnnarch._symboltable.ArchitectureSymbol;
import de.monticore.mlpipelines.ModelLoader;
import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;


@RunWith(MockitoJUnitRunner.class)
public class CandidateASTNodeBuilderTest extends TestCase {
    private final CandidateASTNodeBuilder candidateBuilder = new CandidateASTNodeBuilder();

    public void setUp() throws Exception {
        super.setUp();
    }

    @Test
    public void testBuild() {
        ArchitectureSymbol refArch = ModelLoader.load("src/test/resources/models/adanet/", "adaNetBase");
        ASTArchitecture refAST = (ASTArchitecture) refArch.getAstNode().orElse(null);
        AdaNetCandidate candidate = new AdaNetCandidate(new AdaNetComponent(10), new ArrayList<>());
        ASTArchitecture newArch = candidateBuilder.build(refAST, candidate);
        assertNotNull(newArch);
    }
}