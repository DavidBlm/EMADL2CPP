package de.monticore.mlpipelines.automl.trainalgorithms.efficientnet;

import de.monticore.lang.monticar.cnnarch._symboltable.ArchitectureSymbol;
import de.monticore.mlpipelines.Pipeline;
import de.monticore.mlpipelines.automl.configuration.EfficientNetConfig;
import de.monticore.mlpipelines.automl.trainalgorithms.efficientnet.EfficientNet;
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
        efficientNet.train(startNetwork);
        assertEquals(0.0, efficientNet.trainedAccuracy);
        assertNotNull(efficientNet.getStartNetwork());
    }

    @Test
    public void testTrainCreatesNetworkScaler(){
        EfficientNet efficientNet = createEfficientNet();
        ArchitectureSymbol startNetwork = mock(ArchitectureSymbol.class);
        efficientNet.train(startNetwork);
        assertNotNull(efficientNet.getNetworkScaler());
    }

    @Test
    public void testTrainCreatesGridSearch(){
        EfficientNet efficientNet = createEfficientNet();
        ArchitectureSymbol startNetwork = mock(ArchitectureSymbol.class);
        efficientNet.train(startNetwork);
        assertNotNull(efficientNet.getGridSearch());
    }

    @Test
    public void trainFindsScalingFactors(){
        EfficientNet efficientNet = createEfficientNet();
        ArchitectureSymbol startNetwork = mock(ArchitectureSymbol.class);
        efficientNet.train(startNetwork);
        assertNotNull(efficientNet.getScalingFactors());
    }

    @Test
    public void trainScalesNetwork(){
        EfficientNet efficientNet = createEfficientNet();
        ArchitectureSymbol startNetwork = mock(ArchitectureSymbol.class);
        NetworkScaler networkScaler = mock(NetworkScaler.class);
        efficientNet.setNetworkScaler(networkScaler);
        when(networkScaler.scale(isA(ArchitectureSymbol.class), isA(ScalingFactors.class), any(Integer.class))).thenReturn(startNetwork);
        efficientNet.train(startNetwork);
        assertNotNull(efficientNet.getScaledArchitecture());
    }

    private EfficientNet createEfficientNet(){
        Pipeline pipeline = mock(Pipeline.class);
        EfficientNetConfig config = new EfficientNetConfig();
        EfficientNet efficientNet = new EfficientNet();
        efficientNet.setTrainPipeline(pipeline);
        efficientNet.setTrainConfiguration(config);
        return efficientNet;
    }
}