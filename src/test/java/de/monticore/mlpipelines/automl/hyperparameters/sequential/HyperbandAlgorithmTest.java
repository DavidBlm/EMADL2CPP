package de.monticore.mlpipelines.automl.hyperparameters.sequential;

import conflang._ast.ASTConfLangCompilationUnit;
import conflang._parser.ConfLangParser;
import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

@RunWith(MockitoJUnitRunner.class)
public class HyperbandAlgorithmTest extends TestCase {
    private Optional<ASTConfLangCompilationUnit> initialHyperparams;

    private ASTConfLangCompilationUnit currBestHyperParams;

    private ASTConfLangCompilationUnit newCandidate;

    private ASTConfLangCompilationUnit searchSpace;

    private HyperbandAlgorithm hyperbandAlgorithm1;

    private HyperbandAlgorithm hyperbandAlgorithm2;

    private double evalValue = 0.8;

    private double currEvalValue = 0.7;

    private String metricType = "Accuracy";

    public void setup() throws IOException {
        Path modelPath = Paths.get("src/test/resources/models/automl/optimization_test");
        Path searchSpaceModelPath = Paths.get("src/test/resources/models/automl/searchspaces");
        String model = "Network.conf";

        ConfLangParser parser = new ConfLangParser();
        Path path = Paths.get(modelPath.toString(), model);
        Path searchSpacePath = Paths.get(searchSpaceModelPath.toString(), model);

        currBestHyperParams = parser.parse(path.toString()).get();
        searchSpace = parser.parse(searchSpacePath.toString()).get();

        this.hyperbandAlgorithm1 = new HyperbandAlgorithm();
        initialHyperparams = hyperbandAlgorithm1.getInitialHyperparams(searchSpace);

    }
    @Test
    public void testFirstStepCurrentHyperparameters() {
        assertTrue(hyperbandAlgorithm1.getCurrentHyperparameters().deepEquals(initialHyperparams));
    }

    public void testGetCurrentIteration() {
    }

    public void testSetCurrentIteration() {
    }

    public void testGetCurrBestHyperparams() {
    }

    public void testSetCurrBestHyperparams() {
    }

    public void testGetCurrBestEvalMetric() {
    }

    public void testSetCurrBestEvalMetric() {
    }

    public void testGetInitialHyperparams() {
    }

    public void testCreateValueFromRange() {
    }

    public void testCreateValueFromStep() {
    }

    public void testExecuteOptimizationStep() {
    }

    public void testTop_configurations() {
    }

    public void testGetFullSetOfNewHyperparamsCandidate() {
    }

    public void testGetNewHyperparamsCandidate() {

    }
}