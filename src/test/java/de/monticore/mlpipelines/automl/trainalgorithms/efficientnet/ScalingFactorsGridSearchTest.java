package de.monticore.mlpipelines.automl.trainalgorithms.efficientnet;

import de.monticore.lang.monticar.cnnarch._symboltable.ArchitectureSymbol;
import de.monticore.mlpipelines.automl.configuration.EfficientNetConfig;
import de.monticore.mlpipelines.python.PythonPipeline;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class ScalingFactorsGridSearchTest extends TestCase {
    private ArchitectureSymbol architecture;
    private EfficientNetConfig configuration;
    private ScalingFactorsGridSearch scalingFactorsGridSearch;
    private PythonPipeline pythonPipeline;

    @Before
    public void setUp() {
        architecture = new ArchitectureSymbol();
        configuration = createEfficientNetConfig();
        pythonPipeline = mock(PythonPipeline.class);
        NetworkScaler networkScaler = mock(NetworkScaler.class);
        scalingFactorsGridSearch = new ScalingFactorsGridSearch(architecture, configuration, pythonPipeline, networkScaler);
    }


    @Test
    public void testFindScalingFactors() {
        when(pythonPipeline.getTrainedAccuracy()).thenReturn(0.5f);

        ScalingFactors result = this.scalingFactorsGridSearch.findScalingFactors();
        assertEquals(2.0, result.alpha, 0.001);
        assertEquals(1.0, result.beta, 0.001);
        assertEquals(1.0, result.gamma, 0.001);
    }

    @Test
    public void testFindScalingFactorsReturnsInitialFactors(){
        when(pythonPipeline.getTrainedAccuracy()).thenReturn(0.0f);

        ScalingFactors result = this.scalingFactorsGridSearch.findScalingFactors();
        assertEquals(1.0, result.alpha, 0.001);
        assertEquals(1.0, result.beta, 0.001);
        assertEquals(1.0, result.gamma, 0.001);
    }

    private EfficientNetConfig createEfficientNetConfig() {
        EfficientNetConfig config = new EfficientNetConfig();
        config.setSaveTrainedArchitecture(false);
        config.setArchitectureSavePath("");
        config.setTrainAlgorithmName("EfficientNet");
        config.setTrainPipelineName("Pytorch");

        config.setFlopsConditionValue(2.0);
        config.setMinScalingFactors(new ScalingFactors(1.0, 1.0, 1.0));
        config.setMaxScalingFactors(new ScalingFactors(2.0, 1.4, 1.4));
        config.setScalingFactorsStepSize(new ScalingFactors(0.1, 0.1, 0.1));
        config.setMaximumImageWidthAndHeight(32);
        config.setMinimumImageWidthAndHeight(8);
        config.setPhi(1);

        return config;
    }
}