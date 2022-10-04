package de.monticore.mlpipelines.automl;

import de.monticore.lang.monticar.cnnarch._symboltable.ArchitectureSymbol;
import de.monticore.mlpipelines.automl.configuration.Configuration;
import junit.framework.TestCase;
import org.junit.Test;

public class AutoMLPipelineTest extends TestCase {
    public void testConstructor() {
        AutoMLPipeline automl = new AutoMLPipeline();
        assertNotNull(automl);
    }

    @Test
    public void testTrainLoadsConfig() {
        AutoMLPipeline automl = new AutoMLPipeline();
        ArchitectureSymbol architecture = new ArchitectureSymbol();
        Configuration configuration = new Configuration();
        automl.train(architecture, configuration);
        assertNotNull(automl.getConfiguration());
    }

     @Test
    public void testTrainLoadsArchitecture() {
        AutoMLPipeline automl = new AutoMLPipeline();
        ArchitectureSymbol architecture = new ArchitectureSymbol();
        Configuration configuration = new Configuration();
        automl.train(architecture, configuration);
        assertNotNull(automl.getArchitecture());
    }

    @Test
    public void testTrainCreatesTrainAlgorithm() {
        AutoMLPipeline automl = new AutoMLPipeline();
        ArchitectureSymbol architecture = new ArchitectureSymbol();
        Configuration configuration = new Configuration();
        automl.train(architecture, configuration);
        assertNotNull(automl.getTrainAlgorithm());
    }
}