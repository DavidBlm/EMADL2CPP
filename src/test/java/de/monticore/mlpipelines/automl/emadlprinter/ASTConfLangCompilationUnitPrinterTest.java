package de.monticore.mlpipelines.automl.emadlprinter;

import conflang._ast.ASTConfLangCompilationUnit;
import conflang._parser.ConfLangParser;
import de.monticore.mlpipelines.automl.helper.ASTConfLangCompilationUnitHandler;
import de.monticore.mlpipelines.automl.helper.FileLoader;
import junit.framework.TestCase;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;


public class ASTConfLangCompilationUnitPrinterTest extends TestCase {

    private ASTConfLangCompilationUnit getCompilationUnit(String modelPath, String model) {
        ConfLangParser parser = new ConfLangParser();
        Path path = Paths.get(modelPath, model);
        ASTConfLangCompilationUnit compilationUnit = null;
        try {
            compilationUnit = parser.parse(path.toString()).get();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return compilationUnit;
    }

    public void testPrettyPrintNetworkConf() {
        String modelPath = "src/test/resources/models/automl";
        String model = "Network.conf";
        ASTConfLangCompilationUnit compilationUnit = this.getCompilationUnit(modelPath, model);

        ASTConfLangCompilationUnitPrinter printer = new ASTConfLangCompilationUnitPrinter();
        String[] conf = printer.prettyPrint(compilationUnit).split("\n");

        List<String> expectedConf = FileLoader.loadFile("src/test/resources/models/automl/Network.conf");
        assertEquals(expectedConf.size(), conf.length);
        for (int i = 0; i < conf.length; i++) {
            System.out.println(conf[i]);
            assertEquals(expectedConf.get(i), conf[i]);
        }
    }

    public void testPrettyPrintAdaptedNetworkConf() {
        String modelPath = "src/test/resources/models/automl";
        String model = "Network.conf";
        ASTConfLangCompilationUnit compilationUnit = this.getCompilationUnit(modelPath, model);

        ASTConfLangCompilationUnitHandler.setValueForKey(compilationUnit, "num_epoch", 100);

        ASTConfLangCompilationUnitPrinter printer = new ASTConfLangCompilationUnitPrinter();
        String[] conf = printer.prettyPrint(compilationUnit).split("\n");

        List<String> expectedConf = FileLoader.loadFile("src/test/resources/models/automl/adapted/Network.conf");
        assertEquals(expectedConf.size(), conf.length);
        for (int i = 0; i < conf.length; i++) {
            System.out.println(conf[i]);
            assertEquals(expectedConf.get(i), conf[i]);
        }
    }

    public void testPrettyPrintEfficientNetConf() {
        String modelPath = "src/test/resources/models/automl";
        String model = "EfficientNet.conf";
        ASTConfLangCompilationUnit compilationUnit = this.getCompilationUnit(modelPath, model);

        ASTConfLangCompilationUnitPrinter printer = new ASTConfLangCompilationUnitPrinter();
        String[] conf = printer.prettyPrint(compilationUnit).split("\n");

        List<String> expectedConf = FileLoader.loadFile("src/test/resources/models/automl/EfficientNet.conf");
        assertEquals(expectedConf.size(), conf.length);
        for (int i = 0; i < conf.length; i++) {
            System.out.println(conf[i]);
            assertEquals(expectedConf.get(i), conf[i]);
        }
    }

    public void testPrettyPrintEvaluationCriteriaConf() {
        String modelPath = "src/test/resources/models/automl";
        String model = "EvaluationCriteria.conf";
        ASTConfLangCompilationUnit compilationUnit = this.getCompilationUnit(modelPath, model);

        ASTConfLangCompilationUnitPrinter printer = new ASTConfLangCompilationUnitPrinter();
        String[] conf = printer.prettyPrint(compilationUnit).split("\n");

        List<String> expectedConf = FileLoader.loadFile("src/test/resources/models/automl/EvaluationCriteria.conf");
        assertEquals(expectedConf.size(), conf.length);
        for (int i = 0; i < conf.length; i++) {
            System.out.println(conf[i]);
            assertEquals(expectedConf.get(i), conf[i]);
        }
    }

    public void testPrettyPrintHyperparameterOptConf() {
        String modelPath = "src/test/resources/models/automl";
        String model = "HyperparameterOpt.conf";
        ASTConfLangCompilationUnit compilationUnit = this.getCompilationUnit(modelPath, model);

        ASTConfLangCompilationUnitPrinter printer = new ASTConfLangCompilationUnitPrinter();
        String[] conf = printer.prettyPrint(compilationUnit).split("\n");

        List<String> expectedConf = FileLoader.loadFile("src/test/resources/models/automl/HyperparameterOpt.conf");
        assertEquals(expectedConf.size(), conf.length);
        for (int i = 0; i < conf.length; i++) {
            System.out.println(conf[i]);
            assertEquals(expectedConf.get(i), conf[i]);
        }
    }
}
