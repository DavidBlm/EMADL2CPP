package de.monticore.mlpipelines.parser;

import de.monticore.mlpipelines.automl.configuration.InitialHyperparameters;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Hashtable;

public class InitialHyperparametersParserTest extends TestCase {

    private InitialHyperparameters initialHyperparameters;

    private Hashtable<String, Object> optimizerConfig;

    @Before
    public void setUp() throws Exception {
        Path modelPath = Paths.get("src/test/resources/models/automl");
        String modelName = "Network";
        String scmName = "Supervised";

        InitialHyperparametersParser parser = new InitialHyperparametersParser();
        initialHyperparameters = parser.getConfiguration(modelPath, modelName, scmName);
        optimizerConfig  = initialHyperparameters.getOptimizer();
    }

    @Test
    public void testInitialHyperparametersNumEpochIs8() {
        assertEquals(initialHyperparameters.getNumEpochs(), 8);
    }

    @Test
    public void testInitialHyperparametersBatchSize(){
        assertEquals(initialHyperparameters.getBatchSize(), 10);
    }

    @Test
    public void testInitialHyperparametersNotNormalize(){
        assertFalse(initialHyperparameters.isNormalize());
    }

    @Test
    public void testInitialHyperparametersContextIsCPU(){
        assertEquals(initialHyperparameters.getContext(), "cpu");
    }

    @Test
    public void testInitialHyperparametersNotLoadCheckpoint(){
        assertFalse(initialHyperparameters.isLoadCheckpoint());
    }

    @Test
    public void testOptimizerConfigOptimizerIsAdam(){
        assertEquals(optimizerConfig.get("optimizer"), "adam");
    }

    @Test
    public void testOptimizerConfigLearningRateIs0_001(){
        assertEquals(optimizerConfig.get("learning_rate"), 0.001);
    }

    @Test
    public void testOptimizerConfigLearningRateDecayIs0_8(){
        assertEquals(optimizerConfig.get("learning_rate_decay"), 0.8);
    }

    @Test
    public void testOptimizerConfigStepSizeIs1000(){
        assertEquals(optimizerConfig.get("step_size"), 1000);
    }

    @Test
    public void testOptimizerConfigWeightDecayIs0(){
        assertEquals(optimizerConfig.get("weight_decay"), 0.0);
    }

}
