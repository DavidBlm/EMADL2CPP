package de.monticore.mlpipelines.automl.emadlprinter;

import de.monticore.lang.monticar.cnnarch._ast.ASTArchitecture;
import de.monticore.lang.monticar.cnnarch._symboltable.ArchitectureSymbol;
import de.monticore.mlpipelines.ModelLoader;
import junit.framework.TestCase;

public class EmadlPrettyPrinterTest extends TestCase {

    public void testPrettyPrint() {
        ArchitectureSymbol arch = ModelLoader.loadEfficientnetB0();
        EmadlPrettyPrinter printer = new EmadlPrettyPrinter();
        ASTArchitecture ast = (ASTArchitecture) arch.getAstNode().get();
    }

    public void testVisit() {
    }
}