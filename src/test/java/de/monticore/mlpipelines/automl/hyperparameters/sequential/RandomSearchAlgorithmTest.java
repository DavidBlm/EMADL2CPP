package de.monticore.mlpipelines.automl.hyperparameters.sequential;

import conflang._ast.ASTConfLangCompilationUnit;
import conflang._parser.ConfLangParser;
import de.monticore.mlpipelines.automl.emadlprinter.ASTConfLangCompilationUnitPrinter;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.nio.file.Path;
import java.nio.file.Paths;
@RunWith(MockitoJUnitRunner.class)
public class RandomSearchAlgorithmTest extends TestCase {
    private ASTConfLangCompilationUnit searchSpace;
    private RandomSearchAlgorithm randomSearchAlgorithm;


    @Before
    public void setUp() throws Exception {
        Path modelPath = Paths.get("src/test/resources/models/automl/optimization_test");
        Path searchSpaceModelPath = Paths.get("src/test/resources/models/automl/searchspaces");
        String model = "Network.conf";

        ConfLangParser parser = new ConfLangParser();
        Path path = Paths.get(modelPath.toString(), model);
        Path searchSpacePath = Paths.get(searchSpaceModelPath.toString(), model);

        searchSpace = parser.parse(searchSpacePath.toString()).get();

        this.randomSearchAlgorithm = new RandomSearchAlgorithm();


    }

    @Test
    public void testGetNewHyperparamsCandidate() {
        ASTConfLangCompilationUnit configuration = randomSearchAlgorithm.getNewHyperparamsCandidate(searchSpace);
        assertNotNull(configuration);
        ASTConfLangCompilationUnitPrinter printer = new ASTConfLangCompilationUnitPrinter();
        System.out.println(printer.prettyPrint(configuration));
    }


}