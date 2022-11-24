package de.monticore.mlpipelines.automl.trainalgorithms.adanet.builder;

import de.monticore.ast.ASTNode;
import de.monticore.lang.monticar.cnnarch._ast.ASTArchitecture;
import de.monticore.lang.monticar.cnnarch._ast.ASTArchitectureElement;
import de.monticore.lang.monticar.cnnarch._ast.ASTParallelBlock;
import de.monticore.lang.monticar.cnnarch._ast.ASTStream;
import de.monticore.lang.monticar.cnnarch._symboltable.ArchitectureElementSymbol;
import de.monticore.lang.monticar.cnnarch._symboltable.ArchitectureSymbol;
import de.monticore.lang.monticar.cnnarch._symboltable.SerialCompositeElementSymbol;
import de.monticore.lang.monticar.cnnarch._symboltable.StreamInstructionSymbol;
import de.monticore.mlpipelines.ModelLoader;
import de.monticore.mlpipelines.automl.trainalgorithms.adanet.models.AdaNetCandidate;
import de.monticore.mlpipelines.automl.trainalgorithms.adanet.models.AdaNetComponent;
import junit.framework.TestCase;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CandidateBuilderTest extends TestCase {

    @Test
    public void testConstructor() {
        CandidateBuilder candidateBuilder = new CandidateBuilder();
        assertNotNull(candidateBuilder);
    }

    @Test
    public void testBuild() {
        CandidateBuilder candidateBuilder = new CandidateBuilder();
        ArchitectureSymbol originalArchitecture = ModelLoader.loadAdaNetBase();
        AdaNetCandidate candidate = new AdaNetCandidate(new AdaNetComponent(3), new ArrayList<>());

        ArchitectureSymbol candidateArchitecture = candidateBuilder.build(candidate, originalArchitecture);
        Optional<ASTNode> astNode = candidateArchitecture.getAstNode();
        ASTArchitecture astArch = (ASTArchitecture) astNode.get();
        assertNotNull(candidateArchitecture);
        assertNotNull(astArch);
    }

    @Test
    public void testBuildReplacesAdaNetAST() {
        CandidateBuilder candidateBuilder = new CandidateBuilder();
        ArchitectureSymbol originalArchitecture = ModelLoader.loadAdaNetBase();
        AdaNetCandidate candidate = new AdaNetCandidate(new AdaNetComponent(1), new ArrayList<>());

        ArchitectureSymbol candidateArchitecture = candidateBuilder.build(candidate, originalArchitecture);
        StreamInstructionSymbol networkInstructionSymbol =
                (StreamInstructionSymbol) candidateArchitecture.getNetworkInstructions().get(0);
        SerialCompositeElementSymbol body = networkInstructionSymbol.getBody();
        List<ArchitectureElementSymbol> symbolElements = body.getElements();
        ASTStream astNode = (ASTStream) body.getAstNode().get();
        List<ASTArchitectureElement> astElements = astNode.getElementsList();
        assertEquals(4, astElements.size());
        assertTrue(astElements.get(1) instanceof ASTParallelBlock);
    }
}