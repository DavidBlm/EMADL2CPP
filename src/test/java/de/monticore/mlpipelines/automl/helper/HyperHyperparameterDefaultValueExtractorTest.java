package de.monticore.mlpipelines.automl.helper;

import junit.framework.TestCase;

public class HyperHyperparameterDefaultValueExtractorTest extends TestCase {

    String schemaPath = "src/test/resources/models/automl/schemas/HyperparameterOpt.scm";

    public void testSaInitialTemperature() {
        double initialTemperature = (double) HyperHyperparameterDefaultValueExtractor.getDefaultValue(schemaPath,
                "SA", "initial_temperature");
        assertEquals(initialTemperature, 50.0);
    }
}
