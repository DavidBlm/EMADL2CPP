package de.monticore.mlpipelines.automl.trainalgorithms.adanet.builder;

import de.monticore.lang.monticar.cnnarch._ast.ASTParallelBlock;
import de.monticore.lang.monticar.cnnarch._symboltable.ArchitectureElementSymbol;
import de.monticore.mlpipelines.automl.trainalgorithms.adanet.models.AdaNetCandidate;
import de.monticore.mlpipelines.automl.trainalgorithms.adanet.models.AdaNetComponent;
import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;

import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public class CandidateASTBuilderTest extends TestCase {

    @Test
    public void testConstructor() {
        CandidateASTBuilder candidateASTBuilder = new CandidateASTBuilder();
        assertNotNull(candidateASTBuilder);
    }

    @Test
    public void testBuildOutputsNotNull() {
        CandidateASTBuilder candidateASTBuilder = new CandidateASTBuilder();
        AdaNetCandidate candidate = new AdaNetCandidate(new AdaNetComponent(3), new ArrayList<>());
        ArchitectureElementSymbol adanetSymbol = mock(ArchitectureElementSymbol.class);
        ASTParallelBlock adanetAst = candidateASTBuilder.build(candidate, adanetSymbol);
        assertNotNull(adanetAst);
    }

    @Test
    public void testBuildCreatesValidAst() {
        CandidateASTBuilder candidateASTBuilder = new CandidateASTBuilder();
        AdaNetCandidate candidate = new AdaNetCandidate(new AdaNetComponent(3), new ArrayList<>());
        CandidateSymbolBuilder candidateSymbolBuilder = new CandidateSymbolBuilder();
        ArchitectureElementSymbol adanetSymbol = candidateSymbolBuilder.build(candidate);

        ASTParallelBlock adanetAst = candidateASTBuilder.build(candidate, adanetSymbol);
        assertNotNull(adanetAst);
    }
}