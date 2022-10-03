package de.monticore.mlpipelines.automl;

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
        automl.train();
        assertNotNull(automl.getConfiguration());
    }

     @Test
    public void testTrainLoadsArchitecture() {
        AutoMLPipeline automl = new AutoMLPipeline();
        automl.train();
        assertNotNull(automl.getArchitecture());
    }

    @Test
    public void testTrainCreatesTrainAlgorithm() {
        AutoMLPipeline automl = new AutoMLPipeline();
        automl.train();
        assertNotNull(automl.getTrainAlgorithm());
    }
}