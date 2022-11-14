package de.monticore.helper;

import conflang.ConfLangFacade;
import conflang._symboltable.ConfigurationScope;
import de.monticore.mlpipelines.automl.helper.ConfigurationScopeHandler;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@RunWith(MockitoJUnitRunner.class)
public class ConfigurationScopeHandlerTest extends TestCase {

    ConfigurationScope configurationScope;

    @Before
    public void setup() {
        Path modelPath = Paths.get("src/test/resources/models/automl");
        String modelName = "Network.conf";
        configurationScope = ConfLangFacade.create(modelPath, modelName).getArtifactScope();
    }

    @Test
    public void testGetNumEpochValue() {
        int numEpoch = (int) ConfigurationScopeHandler.getValueByKey(this.configurationScope, "num_epoch");
        assertEquals(numEpoch, 8);
    }

    @Test
    public void testGetBatchSizeValue() {
        int batchSize = (int) ConfigurationScopeHandler.getValueByKey(this.configurationScope, "batch_size");
        assertEquals(batchSize, 10);
    }

    @Test
    public void testGetNormalizeValue() {
        boolean normalize = (boolean) ConfigurationScopeHandler.getValueByKey(this.configurationScope, "normalize");
        assertFalse(normalize);
    }

    @Test
    public void testGetContextValue() {
        String context = (String) ConfigurationScopeHandler.getValueByKey(this.configurationScope, "context");
        assertEquals(context, "cpu");
    }

    @Test
    public void testGetNestedOptimizerValue() {
        Map<String, Object> configMap = ConfigurationScopeHandler.getValuesFromNestedConfiguration(this.configurationScope, "optimizer");
        Map<String, Object> nestedMap = (Map<String, Object>) configMap.get("nestedMap");

        assertEquals(configMap.get("optimizer"), "adam");
        assertEquals(nestedMap.get("learning_rate"), 0.001);
        assertEquals(nestedMap.get("learning_rate_decay"), 0.8);
        assertEquals(nestedMap.get("step_size"), 1000);
        assertEquals(nestedMap.get("weight_decay"), 0.00);
    }

    @Test
    public void testSetValueForKey() {
        ConfigurationScope updatedScope = ConfigurationScopeHandler.setValueForKey(this.configurationScope, "num_epoch", 50);
        int updatedNumEpoch = (int) ConfigurationScopeHandler.getValueByKey(updatedScope, "num_epoch");

        assertEquals(updatedNumEpoch, 50);
    }

    @Test
    public void testSetNestedValueForKeys() {
        ConfigurationScope updatedScope = ConfigurationScopeHandler.setNestedValueForKeys(this.configurationScope, "optimizer", "learning_rate", 0.005);
        Map<String, Object> updatedConfigMap = ConfigurationScopeHandler.getValuesFromNestedConfiguration(updatedScope, "optimizer");
        Map<String, Object> updatedNestedMap = (Map<String, Object>) updatedConfigMap.get("nestedMap");
        double updatedLearningRate = (double) updatedNestedMap.get("learning_rate");

        assertEquals(updatedLearningRate, 0.005);
    }
}
