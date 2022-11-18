package de.monticore.mlpipelines.python;

import junit.framework.TestCase;

public class PythonPipelineTest extends TestCase {
    public void testConstructor() {
        PythonPipeline pythonPipeline = new PythonPipeline();
        assertNotNull(pythonPipeline);
    }
}