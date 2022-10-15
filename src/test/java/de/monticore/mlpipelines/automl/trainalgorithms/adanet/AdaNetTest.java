package de.monticore.mlpipelines.automl.trainalgorithms.adanet;

import junit.framework.TestCase;
import org.junit.Test;

public class AdaNetTest extends TestCase {
    @Test
    public void testConstructor() {
        AdaNet adaNet = new AdaNet();
        assertNotNull(adaNet);
    }
}