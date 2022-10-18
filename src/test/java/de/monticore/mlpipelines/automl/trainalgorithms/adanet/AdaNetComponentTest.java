package de.monticore.mlpipelines.automl.trainalgorithms.adanet;

import junit.framework.TestCase;

public class AdaNetComponentTest extends TestCase {

    public void testConstructor() {
        AdaNetComponent adaNetComponent = new AdaNetComponent(10);
        assertNotNull(adaNetComponent);
    }

    public void testGetNumberLayers() {
        AdaNetComponent adaNetComponent = new AdaNetComponent(10);
        assertEquals(10, adaNetComponent.getNumberLayers());
    }

    public void testSetNumberLayers() {
        AdaNetComponent adaNetComponent = new AdaNetComponent(10);
        adaNetComponent.setNumberLayers(20);
        assertEquals(20, adaNetComponent.getNumberLayers());
    }
}