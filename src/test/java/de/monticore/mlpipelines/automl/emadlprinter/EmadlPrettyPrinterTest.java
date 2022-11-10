package de.monticore.mlpipelines.automl.emadlprinter;

import de.monticore.lang.monticar.cnnarch._ast.ASTArchitecture;
import de.monticore.lang.monticar.cnnarch._symboltable.ArchitectureSymbol;
import de.monticore.mlpipelines.ModelLoader;
import junit.framework.TestCase;

public class EmadlPrettyPrinterTest extends TestCase {

    public void testPrettyPrintArchitectureSymbol() {
        ArchitectureSymbol arch = ModelLoader.loadEfficientnetB0();
        EmadlPrettyPrinter printer = new EmadlPrettyPrinter();
        String emadl = printer.prettyPrint(arch);
        String expectedEmadl = "";
        assertEquals(expectedEmadl, emadl);
    }

    public void testPrettyPrintAstArchitecture() {
        ArchitectureSymbol arch = ModelLoader.loadEfficientnetB0();
        EmadlPrettyPrinter printer = new EmadlPrettyPrinter();
        ASTArchitecture ast = (ASTArchitecture) arch.getAstNode().get();
        String emadl = printer.prettyPrint(ast);
        String expectedEmadl = "";
        assertEquals(expectedEmadl, emadl);
    }

    public void testVisit() {
    }
}