package de.monticore.mlpipelines.automl.hyperparameters.sequential;

import junit.framework.TestCase;

public class SimulatedAnnealingTest extends TestCase {

    public void testConstructor() {
        SimulatedAnnealing simulatedAnnealing = new SimulatedAnnealing();
        assertNotNull(simulatedAnnealing);
    }

    public void testDecideAcceptance() {
    }

    public void testDecreaseTemperature() {
    }
}