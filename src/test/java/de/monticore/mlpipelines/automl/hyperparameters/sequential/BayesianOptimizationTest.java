package de.monticore.mlpipelines.automl.hyperparameters.sequential;

import junit.framework.TestCase;

public class BayesianOptimizationTest extends TestCase {

    public void testConstructor() {
        BayesianOptimization bayesianOptimization = new BayesianOptimization();
        assertNotNull(bayesianOptimization);
    }

    public void testApproximateFunct() {
    }
}