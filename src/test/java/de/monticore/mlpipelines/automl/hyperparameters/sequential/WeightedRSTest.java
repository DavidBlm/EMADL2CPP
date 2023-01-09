package de.monticore.mlpipelines.automl.hyperparameters.sequential;

import junit.framework.TestCase;

public class WeightedRSTest extends TestCase {

    public void testConstructor() {
        WeightedRS weightedRS = new WeightedRS();
        assertNotNull(weightedRS);
    }

    public void testDetermineWeights() {
    }
}