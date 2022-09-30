package de.monticore.mlpipelines.parser;

import de.monticore.mlpipelines.automl.Configuration;
import de.monticore.mlpipelines.automl.TrainAlgorithmConfig;
import junit.framework.TestCase;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Hashtable;

public class ConfFile2ConfigurationParserTest extends TestCase {

    public void testParseConfFile2Configuration() {
        Path modelPath = Paths.get("src/test/resources/models/automl");
        String modelName = "AutoMLExample.conf";

        ConfFile2ConfigurationParser parser = new ConfFile2ConfigurationParser(modelPath, modelName);
        Configuration configuration = parser.getConfiguration();

        Hashtable<String, Object> preprocessingConfig =  configuration.getPreprocessingConfig();
        String hyperparameterOptimizerConfig = configuration.getHyperparameterOptimizerConfig();
        Hashtable<String, Object> evaluationConfig = configuration.getEvaluationConfig();
        Hashtable<String, Object> networkConfig = configuration.getNetworkConfig();
        Hashtable<String, Object> initialHyperparameters = configuration.getInitialHyperparameters();
        Hashtable<String, Object> optimizerConfig  = (Hashtable<String, Object>) initialHyperparameters.get("optimizer");
        TrainAlgorithmConfig trainAlgorithmConfig = configuration.getTrainAlgorithmConfig();

        // Test preprocessing config
        assertEquals(preprocessingConfig.get("train_split"), 0.8);
        assertEquals(preprocessingConfig.get("norm_method"), "normalization");
        assertTrue((Boolean) preprocessingConfig.get("grayscale"));
        assertTrue((Boolean) preprocessingConfig.get("data_augmentation"));
        // Test hyperparameter optimizer config
        assertEquals(hyperparameterOptimizerConfig, "SA");
        // Test preprocessing config
        assertEquals(evaluationConfig.get("metric"), "accuracy");
        assertEquals(evaluationConfig.get("acceptance_rate"), 0.9);
        // Test network config
        assertEquals(networkConfig.get("some_key"), "Some Value");
        // Test initialHyperparameters config
        assertEquals(initialHyperparameters.get("num_epoch"), 8);
        assertEquals(initialHyperparameters.get("batch_size"), 10);
        assertFalse((Boolean) initialHyperparameters.get("normalize"));
        assertEquals(initialHyperparameters.get("context"), "cpu");
        assertFalse((Boolean) initialHyperparameters.get("load_checkpoint"));
        assertEquals(optimizerConfig.get("optimizer"), "adam");
        assertEquals(optimizerConfig.get("learning_rate"), 0.001);
        assertEquals(optimizerConfig.get("learning_rate_decay"), 0.8);
        assertEquals(optimizerConfig.get("step_size"), 1000);
        assertEquals(optimizerConfig.get("weight_decay"), 0.00);
        // Test trainAlgorithmConfig
        assertEquals(trainAlgorithmConfig.getNumEpochs(), 1);
        assertFalse(trainAlgorithmConfig.isSaveTrainedArchitecture());
        assertEquals(trainAlgorithmConfig.getArchitectureSavePath(), "");
    }
}
