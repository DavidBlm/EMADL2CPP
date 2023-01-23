package de.monticore.mlpipelines.automl;

import de.monticore.lang.monticar.cnnarch._symboltable.ArchitectureSymbol;
import de.monticore.mlpipelines.automl.configuration.Configuration;
import de.monticore.mlpipelines.automl.trainalgorithms.NeuralArchitectureSearchBuilder;
import de.monticore.mlpipelines.automl.trainalgorithms.efficientnet.EfficientNet;
import junit.framework.TestCase;
import org.junit.Ignore;
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
    @Ignore("Needs some other code to work")
    public void testTrainLoadsArchitecture() {
        AutoMLPipeline automl = getAutoMLPipeline();
        ArchitectureSymbol architecture = new ArchitectureSymbol();
        Configuration configuration = new Configuration();
        automl.execute(architecture, configuration);
        assertNotNull(automl.getArchitecture());
    }

    @Test
    @Ignore("Needs some other code to work")
    public void testTrainCreatesTrainAlgorithm() {
        AutoMLPipeline automl = getAutoMLPipeline();
        ArchitectureSymbol architecture = new ArchitectureSymbol();
        Configuration configuration = new Configuration();
        automl.execute(architecture, configuration);
        assertNotNull(automl.getTrainAlgorithm());
    }

    private static AutoMLPipeline getAutoMLPipeline() {
        AutoMLPipeline automl = new AutoMLPipeline();
        NeuralArchitectureSearchBuilder builder = mock(NeuralArchitectureSearchBuilder.class);
        EfficientNet efficientNet = mock(EfficientNet.class);
        doReturn(null).when(efficientNet).execute(isA(ArchitectureSymbol.class));
        when(builder.build()).thenReturn(efficientNet);
        automl.setTrainAlgorithmBuilder(builder);
        return automl;
    }
}