package de.monticore.mlpipelines.automl.emadlprinter;

import de.monticore.lang.monticar.cnnarch._symboltable.ArchitectureSymbol;
import de.monticore.mlpipelines.ModelLoader;
import de.monticore.mlpipelines.automl.helper.FileLoader;
import junit.framework.TestCase;

import java.util.List;

public class EmadlPrettyPrinterTest extends TestCase {
    public void testPrettyPrintEfficientnet() {
        ArchitectureSymbol arch = ModelLoader.loadEfficientnetB0();
        EmadlPrettyPrinter printer = new EmadlPrettyPrinter();
        String[] emadl = printer.prettyPrint(arch).split("\n");

        List<String> expectedEmadl = FileLoader.loadFile("src/test/resources/models/EfficientNetB0.emadl");
        assertEquals(expectedEmadl.size(), emadl.length);
        for (int i = 0; i < emadl.length; i++) {
            System.out.println(emadl[i]);
            assertEquals(expectedEmadl.get(i), emadl[i]);
        }
    }

    public void testPrettyPrintAdanetStart() {
        ArchitectureSymbol arch = ModelLoader.load("src/test/resources/models/adanet/", "adaNetStart");
        EmadlPrettyPrinter printer = new EmadlPrettyPrinter();
        String[] emadl = printer.prettyPrint(arch).split("\n");

        List<String> expectedEmadl = FileLoader.loadFile("src/test/resources/models/adanet/AdaNetStart.emadl");
        assertEquals(expectedEmadl.size(), emadl.length);
        for (int i = 0; i < emadl.length; i++) {
            System.out.println(emadl[i]);
            assertEquals(expectedEmadl.get(i), emadl[i]);
        }
    }

    public void testPrettyPrintAdanetBase() {
        ArchitectureSymbol arch = ModelLoader.load("src/test/resources/models/adanet/", "adaNetBase");
        EmadlPrettyPrinter printer = new EmadlPrettyPrinter();
        String[] emadl = printer.prettyPrint(arch).split("\n");

        List<String> expectedEmadl = FileLoader.loadFile("src/test/resources/models/adanet/EfficientNet.emadl");
        assertEquals(expectedEmadl.size(), emadl.length);
        for (int i = 0; i < emadl.length; i++) {
            System.out.println(emadl[i]);
            assertEquals(expectedEmadl.get(i), emadl[i]);
        }
    }
}