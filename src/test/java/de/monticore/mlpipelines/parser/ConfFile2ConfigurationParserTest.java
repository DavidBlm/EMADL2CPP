package de.monticore.mlpipelines.parser;

import de.monticore.mlpipelines.automl.configuration.Configuration;
import de.monticore.mlpipelines.automl.configuration.TrainAlgorithmConfig;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Hashtable;

public class ConfFile2ConfigurationParserTest extends TestCase {
    private Path modelPath;
    private String modelName;
    private ConfFile2ConfigurationParser parser;
    private Configuration configuration;
    private String hyperparameterOptimizerConfig;
    private Hashtable<String, Object> preprocessingConfig;
    private Hashtable<String, Object> evaluationConfig;
    private Hashtable<String, Object> networkConfig;
    private Hashtable<String, Object> initialHyperparameters;
    private Hashtable<String, Object> optimizerConfig;
    private TrainAlgorithmConfig trainAlgorithmConfig;

    @Before
    public void setUp() throws Exception {
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
    }

    @Test
    public void testTrainSplit() {
        assertEquals(preprocessingConfig.get("train_split"), 0.8);
    }

    @Test
    public void testNormMethod() {
        assertEquals(preprocessingConfig.get("norm_method"), "normalization");
    }

    @Test
    public void testGrayscale() {
        assertTrue((Boolean) preprocessingConfig.get("grayscale"));
    }

    @Test
    public void testDataAugmentation() {
        assertTrue((Boolean) preprocessingConfig.get("data_augmentation"));
    }

    @Test
    public void testHyperparameterOptimizerConfig() {
        assertEquals(hyperparameterOptimizerConfig, "SA");
    }

    @Test
    public void testEvaluationMetric() {
        assertEquals(evaluationConfig.get("metric"), "accuracy");
    }

    @Test
    public void testAcceptanceRate() {
        assertEquals(evaluationConfig.get("acceptance_rate"), 0.9);
    }

    @Test
    public void testNetworkConfig() {
        assertEquals(networkConfig.get("some_key"), "Some Value");
    }

    @Test
    public void testInitialHyperparametersNumEpochIs8() {
        assertEquals(initialHyperparameters.get("num_epoch"), 8);
    }

    @Test
    public void testInitialHyperparametersBatchSize(){
        assertEquals(initialHyperparameters.get("batch_size"), 10);
    }

    @Test
    public void testInitialHyperparametersNotNormalize(){
        assertFalse((Boolean) initialHyperparameters.get("normalize"));
    }

    @Test
    public void testInitialHyperparametersContextIsCPU(){
        assertEquals(initialHyperparameters.get("context"), "cpu");
    }

    @Test
    public void testInitialHyperparametersNotLoadCheckpoint(){
        assertFalse((Boolean) initialHyperparameters.get("load_checkpoint"));
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

    @Test
    public void testTrainAlgorithmConfigNumEpochsIs1(){
        assertEquals(trainAlgorithmConfig.getNumEpochs(), 1);
    }

    @Test
    public void testTrainAlgorithmConfigNotSaveTrainedArchitecture(){
        assertFalse(trainAlgorithmConfig.isSaveTrainedArchitecture());
    }

    @Test
    public void testTrainAlgorithmConfigArchitectureSavePathIsEmpty(){
        assertEquals(trainAlgorithmConfig.getArchitectureSavePath(), "");
    }
}
