package de.monticore.mlpipelines.automl.hyperparameters.sequential;

import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertNotEquals;

@RunWith(MockitoJUnitRunner.class)
public class SimulatedAnnealingTest extends TestCase {

    private SimulatedAnnealing simulatedAnnealing1;

    private SimulatedAnnealing simulatedAnnealing2;
    private Map<String, Double> hyperParams = new HashMap<String, Double>() {{
        put("num_epoch", 8.0);
        put("batch_size", 10.0);
        put("learning_rate", 0.001);
        put("learning_rate_decay", 0.8);
        put("step_size", 1000.0);
        put("weight_decay", 0.00);
    }};

    private Map<String, Double> currBestHyperParams = new HashMap<String, Double>() {{
        put("num_epoch", 10.0);
        put("batch_size", 31.0);
        put("learning_rate", 0.002);
        put("learning_rate_decay", 0.7);
        put("step_size", 100.0);
        put("weight_decay", 0.0001);
    }};

    private double evalValue = 0.8;

    private double currEvalValue = 0.7;

    private String metricType = "Accuracy";

    @Before
    public void setup() {
        this.simulatedAnnealing1 = new SimulatedAnnealing();
        simulatedAnnealing1.setInitialTemperature(50.0);
        simulatedAnnealing1.executeOptimizationStep(hyperParams, evalValue, metricType);

        simulatedAnnealing2 = new SimulatedAnnealing();
        simulatedAnnealing2.setInitialTemperature(50.0);
        simulatedAnnealing2.setCurrentIteration(1);

        simulatedAnnealing2.setCurrentHyperparameters(currBestHyperParams);
        simulatedAnnealing2.setCurrBestHyperparams(currBestHyperParams);
        simulatedAnnealing2.setCurrEvalMetric(currEvalValue);
        simulatedAnnealing2.setCurrBestEvalMetric(currEvalValue);

        simulatedAnnealing2.executeOptimizationStep(hyperParams, evalValue, metricType);
    }

    @Test
    public void testFirstStepCurrBestHyperparams() {
        assertEquals(simulatedAnnealing1.getCurrBestHyperparams(), hyperParams);
    }

    @Test
    public void testFirstStepCurrBestEvalMetric() {
        assertEquals(simulatedAnnealing1.getCurrBestEvalMetric(), evalValue);
    }

    @Test
    public void testFirstStepCurrentHyperparameters() {
        assertEquals(simulatedAnnealing1.getCurrentHyperparameters(), hyperParams);
    }

    @Test
    public void testFirstStepCurrEvalMetric() {
        assertEquals(simulatedAnnealing1.getCurrEvalMetric(), evalValue);
    }

    @Test
    public void testFirstStepCurrentTemperature() {
        assertEquals(simulatedAnnealing1.getCurrentTemperature(), 25.0);
    }

    @Test
    public void testFirstStepNewHyperparamsCandidate() {
        Map<String, Double> newCandidate = simulatedAnnealing1.getNewHyperparamsCandidate();

        assertEquals(newCandidate.keySet(), hyperParams.keySet());
        assertNotEquals(hyperParams, newCandidate);
    }

    @Test
    public void testSecondStepCurrBestHyperparams() {
        assertEquals(simulatedAnnealing2.getCurrBestHyperparams(), hyperParams);
    }

    @Test
    public void testSecondStepCurrBestEvalMetric() {
        assertEquals(simulatedAnnealing2.getCurrBestEvalMetric(), evalValue);
    }

    @Test
    public void testSecondStepCurrentHyperparameters() {
        assert(simulatedAnnealing2.getCurrentHyperparameters().equals(currBestHyperParams) || simulatedAnnealing2.getCurrentHyperparameters().equals(hyperParams));
    }

    @Test
    public void testSecondStepCurrEvalMetric() {
        assert(simulatedAnnealing2.getCurrEvalMetric() == evalValue || simulatedAnnealing2.getCurrEvalMetric() == currEvalValue);
    }

    @Test
    public void testSecondStepCurrentTemperature() {
        assertEquals(simulatedAnnealing2.getCurrentTemperature(), (50.0/3.0));
    }

    @Test
    public void testSecondStepNewHyperparamsCandidate() {
        Map<String, Double> newCandidate = simulatedAnnealing2.getNewHyperparamsCandidate();

        assertEquals(newCandidate.keySet(), simulatedAnnealing2.getCurrentHyperparameters().keySet());
        assertNotEquals(simulatedAnnealing2.getCurrentHyperparameters(), newCandidate);
    }
}