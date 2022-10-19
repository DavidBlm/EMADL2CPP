package de.monticore.mlpipelines.parser;

import de.monticore.mlpipelines.automl.configuration.EfficientNetConfig;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

public class EfficientNetConfigParserTest extends TestCase {

    private EfficientNetConfig efficientNetConfig;

    @Before
    public void setUp() throws Exception {
        Path modelPath = Paths.get("src/test/resources/models/automl");
        String modelName = "EfficientNet";
        String scmName = "EfficientNet";

        EfficientNetConfigParser parser = new EfficientNetConfigParser();
        efficientNetConfig = parser.getConfiguration(modelPath, modelName, scmName);
    }

    @Test
    public void testEfficientNetConfigNotSaveTrainedArchitecture(){
        assertFalse(efficientNetConfig.isSaveTrainedArchitecture());
    }

    @Test
    public void testEfficientNetConfigArchitectureSavePathIsEmpty(){
        assertEquals(efficientNetConfig.getArchitectureSavePath(), "");
    }

    @Test
    public void testEfficientNetConfigTrainAlgorithmName(){
        assertEquals(efficientNetConfig.getTrainAlgorithmName(), "EfficientNet");
    }

    @Test
    public void testEfficientNetConfigTrainPipelineName(){
        assertEquals(efficientNetConfig.getTrainPipelineName(), "Pytorch");
    }

    @Test
    public void testEfficientNetConfigFlopsConditionValue(){
        assertEquals(efficientNetConfig.getFlopsConditionValue(), 2.0);
    }

    @Test
    public void testEfficientNetConfigMinScalingFactors(){
        assertEquals(efficientNetConfig.getMinScalingFactors().alpha, 1.0);
        assertEquals(efficientNetConfig.getMinScalingFactors().beta, 1.0);
        assertEquals(efficientNetConfig.getMinScalingFactors().gamma, 1.0);
    }

    @Test
    public void testEfficientNetConfigMaxScalingFactors(){
        assertEquals(efficientNetConfig.getMaxScalingFactors().alpha, 2.0);
        assertEquals(efficientNetConfig.getMaxScalingFactors().beta, 1.4);
        assertEquals(efficientNetConfig.getMaxScalingFactors().gamma, 1.4);
    }

    @Test
    public void testEfficientNetConfigScalingFactorsStepSize(){
        assertEquals(efficientNetConfig.getScalingFactorsStepSize().alpha, 0.1);
        assertEquals(efficientNetConfig.getScalingFactorsStepSize().beta, 0.1);
        assertEquals(efficientNetConfig.getScalingFactorsStepSize().gamma, 0.1);
    }

    @Test
    public void testEfficientNetConfigMaximumImageWidthAndHeight(){
        assertEquals(efficientNetConfig.getMaximumImageWidthAndHeight(), 32);
    }

    @Test
    public void testEfficientNetConfigMinimumImageWidthAndHeight(){
        assertEquals(efficientNetConfig.getMinimumImageWidthAndHeight(), 8);
    }

    @Test
    public void testEfficientNetConfigPhi(){
        assertEquals(efficientNetConfig.getPhi(), 1);
    }

}
