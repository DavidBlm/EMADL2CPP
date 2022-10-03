package de.monticore.mlpipelines.automl;

import de.monticore.mlpipelines.automl.efficientnet.EfficientNet;
import junit.framework.TestCase;

public class EfficientNetTest extends TestCase {
    public void testConstructor() {
        EfficientNet efficientNet = new EfficientNet();
        assertNotNull(efficientNet);
    }

    public void testTrain() {
    }
}