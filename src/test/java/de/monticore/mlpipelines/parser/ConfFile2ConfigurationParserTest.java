package de.monticore.mlpipelines.parser;

import de.monticore.mlpipelines.automl.configuration.*;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

public class ConfFile2ConfigurationParserTest extends TestCase {
    private Path modelPath;
    private ConfFile2ConfigurationParser parser;
    private Configuration configuration;
    private HyperparameterOptConfig hyperparameterOptConfig;
    private PreprocessingConfig preprocessingConfig;
    private EvaluationConfig evaluationConfig;
    private InitialHyperparameters initialHyperparameters;
    private Hashtable<String, Object> optimizerConfig;
    private TrainAlgorithmConfig trainAlgorithmConfig;

    @Before
    public void setUp() throws Exception {
        modelPath = Paths.get("src/test/resources/models/automl");
        Map<String, String> confScmConfigMap = createConfScmMap();

        parser = new ConfFile2ConfigurationParser();
        configuration = parser.getConfiguration(modelPath, confScmConfigMap);

        preprocessingConfig =  configuration.getPreprocessingConfig();
        hyperparameterOptConfig = configuration.getHyperparameterOptConfig();
        evaluationConfig = configuration.getEvaluationConfig();
        initialHyperparameters = configuration.getInitialHyperparameters();
        optimizerConfig  = initialHyperparameters.getOptimizer();
        trainAlgorithmConfig = configuration.getTrainAlgorithmConfig();
    }

    @Test
    public void testTrainSplit() {
        assertEquals(preprocessingConfig.getTrainSplit(), 0.8);
    }

    @Test
    public void testNormMethod() {
        assertEquals(preprocessingConfig.getNormMethod(), "normalization");
    }

    @Test
    public void testGrayscale() {
        assertTrue(preprocessingConfig.getGrayscale());
    }

    @Test
    public void testDataAugmentation() {
        assertFalse(preprocessingConfig.getDataAugmentation());
    }

    @Test
    public void testHyperparameterOptConfig() {
        assertEquals(hyperparameterOptConfig.getOptimizer(), "SA");
    }

    @Test
    public void testEvaluationMetric() {
        assertEquals(evaluationConfig.getMetric(), "accuracy");
    }

    @Test
    public void testAcceptanceRate() {
        assertEquals(evaluationConfig.getAcceptanceRate(), 0.9);
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

    @Test
    public void testTrainAlgorithmConfigNotSaveTrainedArchitecture(){
        assertFalse(trainAlgorithmConfig.isSaveTrainedArchitecture());
    }

    @Test
    public void testTrainAlgorithmConfigArchitectureSavePathIsEmpty(){
        assertEquals(trainAlgorithmConfig.getArchitectureSavePath(), "");
    }

    private Map<String, String> createConfScmMap() {
        HashMap<String, String> confScmConfigMap = new HashMap<>();
        confScmConfigMap.put("HyperparameterOptConf", "HyperparameterOpt");
        confScmConfigMap.put("HyperparameterOptScm", "HyperparameterOpt");
        confScmConfigMap.put("EvaluationCriteriaConf", "EvaluationCriteria");
        confScmConfigMap.put("EvaluationCriteriaScm", "EvaluationCriteria");
        confScmConfigMap.put("NetworkConf", "Network");
        confScmConfigMap.put("NetworkScm", "Supervised");
        confScmConfigMap.put("TrainAlgorithmConf", "EfficientNet");
        confScmConfigMap.put("TrainAlgorithmScm", "EfficientNet");
        return confScmConfigMap;
    }
}
