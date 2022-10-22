package de.monticore.mlpipelines.automl;

import de.monticore.lang.monticar.cnnarch._symboltable.ArchitectureSymbol;
import de.monticore.mlpipelines.automl.configuration.Configuration;
import de.monticore.mlpipelines.automl.trainalgorithms.TrainAlgorithmBuilder;
import de.monticore.mlpipelines.automl.trainalgorithms.efficientnet.EfficientNet;
import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AutoMLPipelineTest extends TestCase {
    public void testConstructor() {
        AutoMLPipeline automl = new AutoMLPipeline();
        assertNotNull(automl);
    }

    @Test
    public void testTrainLoadsArchitecture() {
        AutoMLPipeline automl = getAutoMLPipeline();
        ArchitectureSymbol architecture = new ArchitectureSymbol();
        Configuration configuration = new Configuration();
        automl.execute(architecture, configuration);
        assertNotNull(automl.getArchitecture());
    }

    private static AutoMLPipeline getAutoMLPipeline() {
        AutoMLPipeline automl = new AutoMLPipeline();
        TrainAlgorithmBuilder builder = mock(TrainAlgorithmBuilder.class);
        EfficientNet efficientNet = mock(EfficientNet.class);
        doNothing().when(efficientNet).execute(isA(ArchitectureSymbol.class));
        when(builder.build()).thenReturn(efficientNet);
        automl.setTrainAlgorithmBuilder(builder);
        return automl;
    }

    @Test
    public void testTrainCreatesTrainAlgorithm() {
        AutoMLPipeline automl = getAutoMLPipeline();
        ArchitectureSymbol architecture = new ArchitectureSymbol();
        Configuration configuration = new Configuration();
        automl.execute(architecture, configuration);
        assertNotNull(automl.getTrainAlgorithm());
    }
}