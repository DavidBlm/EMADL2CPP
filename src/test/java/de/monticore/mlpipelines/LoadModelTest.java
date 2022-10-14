package de.monticore.mlpipelines;

import de.monticore.ast.ASTNode;
import de.monticore.lang.embeddedmontiarc.embeddedmontiarc._symboltable.instanceStructure.EMAComponentInstanceSymbol;
import de.monticore.lang.math._symboltable.expression.MathNumberExpressionSymbol;
import de.monticore.lang.monticar.cnnarch._symboltable.*;
import de.monticore.symboltable.Scope;
import junit.framework.TestCase;
import org.jscience.mathematics.number.LargeInteger;
import org.jscience.mathematics.number.Rational;
import org.junit.Test;

import java.util.*;

import static de.monticore.lang.monticar.emadl.generator.EMADLAbstractSymtab.createSymTab;

public class LoadModelTest extends TestCase {

    @Test
    public void testLoadModelAsArchitectureSymbol() {
        Scope symTab = createSymTab("src/test/resources/models");
        EMAComponentInstanceSymbol c = symTab.<EMAComponentInstanceSymbol>resolve("efficientNetB0",
                EMAComponentInstanceSymbol.KIND).orElse(null);

        ArchitectureSymbol arch1 = c.getSpannedScope().<ArchitectureSymbol>resolve("", ArchitectureSymbol.KIND).get();

        List<NetworkInstructionSymbol> networkInstructions = arch1.getNetworkInstructions();
        for (NetworkInstructionSymbol networkInstruction : networkInstructions) {
            SerialCompositeElementSymbol networkInstructionBody = networkInstruction.getBody();
            List<ArchitectureElementSymbol> elements = networkInstructionBody.getElements();
            for (ArchitectureElementSymbol element : elements) {
                Optional<ASTNode> nodeOptional = element.getAstNode();
                ArchitectureElementScope spannedscope  =  elements.get(2).getSpannedScope();
                ArrayList inner_element = (ArrayList) spannedscope.getLocalSymbols().get("");
                MathNumberExpressionSymbol oldValue = (MathNumberExpressionSymbol) inner_element.get(3);

                Rational num = oldValue.getValue().getRealNumber();

                oldValue.getValue().setRealNumber(num.times(2));

                int a = 0;
                 /*if(astLayer.getName().equals("residualBlock")){
                    ASTArchExpression expr_rhs = astLayer.getArguments(0).getRhs();
                    ArchSimpleExpressionSymbol rhs_symbol = (ArchSimpleExpressionSymbol) expr_rhs.getSymbol();
                    Optional<MathExpressionSymbol> mathexpression  = rhs_symbol.getMathExpression();
                    MathNumberExpressionSymbol symbol = (MathNumberExpressionSymbol) mathexpression.get();
                    System.out.println(symbol.getValue());
                }*/
            }
        }

        assertNotNull(arch1);
    }
}