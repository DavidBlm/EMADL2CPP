package de.monticore.mlpipelines.automl;

import conflang._ast.ASTConfLangCompilationUnit;
import de.monticore.lang.embeddedmontiarc.embeddedmontiarc._symboltable.instanceStructure.EMAComponentInstanceSymbol;
import de.monticore.lang.monticar.cnnarch._symboltable.ArchitectureSymbol;
import de.monticore.lang.monticar.cnnarch.generator.training.LearningMethod;
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
        AutoMLPipeline automl = new AutoMLPipeline(LearningMethod.SUPERVISED);
        assertNotNull(automl);
    }

    @Test
    @Ignore("Needs some other code to work")
    public void testTrainLoadsArchitecture() {
        AutoMLPipeline automl = getAutoMLPipeline();
        ArchitectureSymbol architecture = new ArchitectureSymbol();
        Configuration configuration = new Configuration();
        automl.setNeuralNetwork(mock(EMAComponentInstanceSymbol.class));
        automl.setConfigurationModel(mock(ASTConfLangCompilationUnit.class));
        automl.setTrainingConfiguration(mock(ASTConfLangCompilationUnit.class));
        automl.execute();
        assertNotNull(automl.getArchitecture());
    }

    private static AutoMLPipeline getAutoMLPipeline() {
        AutoMLPipeline automl = new AutoMLPipeline(LearningMethod.SUPERVISED);
        NeuralArchitectureSearchBuilder builder = mock(NeuralArchitectureSearchBuilder.class);
        EfficientNet efficientNet = mock(EfficientNet.class);
        doReturn(null).when(efficientNet).execute(isA(ArchitectureSymbol.class));
        when(builder.build()).thenReturn(efficientNet);
        automl.setTrainAlgorithmBuilder(builder);
        return automl;
    }

    @Test
    @Ignore("Needs some other code to work")
    public void testTrainCreatesTrainAlgorithm() {
        AutoMLPipeline automl = getAutoMLPipeline();
        automl.setNeuralNetwork(mock(EMAComponentInstanceSymbol.class));
        automl.setConfigurationModel(mock(ASTConfLangCompilationUnit.class));
        automl.setTrainingConfiguration(mock(ASTConfLangCompilationUnit.class));
        automl.execute();
        assertNotNull(automl.getTrainAlgorithm());
    }
}