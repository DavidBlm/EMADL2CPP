package de.monticore.mlpipelines.automl.trainalgorithms.efficientnet;

import de.monticore.lang.monticar.cnnarch._symboltable.ArchitectureSymbol;
import de.monticore.mlpipelines.Pipeline;
import de.monticore.mlpipelines.automl.configuration.EfficientNetConfig;
import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class EfficientNetTest extends TestCase {
    @Test
    public void testConstructor() {
        EfficientNet efficientNet = createEfficientNet();
        assertNotNull(efficientNet);
    }

    @Test
    public void testTrainSets() {
        EfficientNet efficientNet = createEfficientNet();
        ArchitectureSymbol startNetwork = mock(ArchitectureSymbol.class);
        efficientNet.execute(startNetwork);
        assertEquals(0.0, efficientNet.trainedAccuracy);
        assertNotNull(efficientNet.getStartNetwork());
    }

    @Test
    public void testTrainCreatesNetworkScaler(){
        EfficientNet efficientNet = createEfficientNet();
        ArchitectureSymbol startNetwork = mock(ArchitectureSymbol.class);
        efficientNet.execute(startNetwork);
        assertNotNull(efficientNet.getNetworkScaler());
    }

    @Test
    public void testTrainCreatesGridSearch(){
        EfficientNet efficientNet = createEfficientNet();
        ArchitectureSymbol startNetwork = mock(ArchitectureSymbol.class);
        efficientNet.execute(startNetwork);
        assertNotNull(efficientNet.getGridSearch());
    }

    @Test
    public void trainFindsScalingFactors(){
        EfficientNet efficientNet = createEfficientNet();
        ArchitectureSymbol startNetwork = mock(ArchitectureSymbol.class);
        efficientNet.execute(startNetwork);
        assertNotNull(efficientNet.getScalingFactors());
    }

    @Test
    public void trainScalesNetwork(){
        EfficientNet efficientNet = createEfficientNet();
        ArchitectureSymbol startNetwork = mock(ArchitectureSymbol.class);
        NetworkScaler networkScaler = mock(NetworkScaler.class);
        efficientNet.setNetworkScaler(networkScaler);
        when(networkScaler.scale(isA(ArchitectureSymbol.class), isA(ScalingFactors.class),
                any(Integer.class))).thenReturn(startNetwork);
        efficientNet.execute(startNetwork);
        assertNotNull(efficientNet.getScaledArchitecture());
    }

    private EfficientNet createEfficientNet(){
        Pipeline pipeline = mock(Pipeline.class);
        EfficientNetConfig config = createEfficientNetConfig();
        EfficientNet efficientNet = new EfficientNet();
        efficientNet.setTrainPipeline(pipeline);
        efficientNet.setTrainConfiguration(config);
        return efficientNet;
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