package de.monticore.mlpipelines.parser;

import de.monticore.mlpipelines.automl.configuration.HyperparameterOptConfig;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

public class HyperparameterOptConfigParserTest extends TestCase {

    private HyperparameterOptConfig hyperparameterOptConfig;

    @Before
    public void setUp() throws Exception {
        Path modelPath = Paths.get("src/test/resources/models/automl");
        String modelName = "HyperparameterOpt";
        String scmName = "HyperparameterOpt";

        HyperparameterOptConfigParser parser = new HyperparameterOptConfigParser();
        hyperparameterOptConfig = parser.getConfiguration(modelPath, modelName, scmName);
    }

    @Test
    public void testHyperparameterOptConfig() {
        assertEquals(hyperparameterOptConfig.getOptimizer(), "SA");
    }

}
