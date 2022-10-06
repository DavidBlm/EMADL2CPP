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
    public void testTrainLoadsConfig() {
        AutoMLPipeline automl = getAutoMLPipeline();
        ArchitectureSymbol architecture = new ArchitectureSymbol();
        String configurationName = "AutoMLExample.conf";
        automl.train(architecture, configurationName);
        assertNotNull(automl.getConfiguration());
    }

    @Test
    public void testTrainLoadsArchitecture() {
        AutoMLPipeline automl = getAutoMLPipeline();
        ArchitectureSymbol architecture = new ArchitectureSymbol();
        Configuration configuration = new Configuration();
        automl.train(architecture, configuration);
        assertNotNull(automl.getArchitecture());
    }

    @Test
    public void testTrainCreatesTrainAlgorithm() {
        AutoMLPipeline automl = getAutoMLPipeline();
        ArchitectureSymbol architecture = new ArchitectureSymbol();
        Configuration configuration = new Configuration();
        automl.train(architecture, configuration);
        assertNotNull(automl.getTrainAlgorithm());
    }

    private static AutoMLPipeline getAutoMLPipeline() {
        AutoMLPipeline automl = new AutoMLPipeline();
        TrainAlgorithmBuilder builder = mock(TrainAlgorithmBuilder.class);
        EfficientNet efficientNet = mock(EfficientNet.class);
        doNothing().when(efficientNet).train(isA(ArchitectureSymbol.class));
        when(builder.build()).thenReturn(efficientNet);
        return automl;
    }
}