package de.monticore.mlpipelines.automl;

import junit.framework.TestCase;

public class AutoMLPipelineTest extends TestCase {
    public void testConstructor() {
        AutoMLPipeline automl = new AutoMLPipeline();
        assertNotNull(automl);
    }

    public void testTrain() {
    }
}