package de.monticore.mlpipelines.parser;

import de.monticore.mlpipelines.automl.configuration.EvaluationConfig;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

public class EvaluationConfigParserTest extends TestCase {

    private EvaluationConfig evaluationConfig;

    @Before
    public void setUp() throws Exception {
        Path modelPath = Paths.get("src/test/resources/models/automl");
        String modelName = "EvaluationCriteria";
        String scmName = "EvaluationCriteria";

        EvaluationConfigParser parser = new EvaluationConfigParser();
        evaluationConfig = parser.getConfiguration(modelPath, modelName, scmName);
    }

    @Test
    public void testEvaluationMetric() {
        assertEquals(evaluationConfig.getMetric(), "accuracy");
    }

    @Test
    public void testAcceptanceRate() {
        assertEquals(evaluationConfig.getAcceptanceRate(), 0.9);
    }

}
