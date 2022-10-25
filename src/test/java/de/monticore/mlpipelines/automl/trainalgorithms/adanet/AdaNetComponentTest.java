package de.monticore.mlpipelines.automl.trainalgorithms.adanet;

import junit.framework.TestCase;

public class AdaNetComponentTest extends TestCase {

    public void testConstructor() {
        AdaNetComponent adaNetComponent = new AdaNetComponent(10);
        assertNotNull(adaNetComponent);
    }

    public void testConstructorWithLayerWidth() {
        AdaNetComponent adaNetComponent = new AdaNetComponent(10, 20);
        assertNotNull(adaNetComponent);
    }

    public void testGetNumberLayers() {
        AdaNetComponent adaNetComponent = new AdaNetComponent(10);
        assertEquals(10, adaNetComponent.getDepth());
    }

    public void testSetNumberLayers() {
        AdaNetComponent adaNetComponent = new AdaNetComponent(10);
        adaNetComponent.setDepth(20);
        assertEquals(20, adaNetComponent.getDepth());
    }

    public void testGetLayerWidth() {
        AdaNetComponent adaNetComponent = new AdaNetComponent(10, 20);
        assertEquals(20, adaNetComponent.getLayerWidth());
    }

    public void testSetLayerWidth() {
        AdaNetComponent adaNetComponent = new AdaNetComponent(10, 20);
        adaNetComponent.setLayerWidth(30);
        assertEquals(30, adaNetComponent.getLayerWidth());
    }

    public void testGetEmadlDepth() {
        AdaNetComponent adaNetComponent = new AdaNetComponent(10, 20);
        assertEquals(10, adaNetComponent.getEmadl().size());
    }

    public void testGetEmadlLastLayer() {
        AdaNetComponent adaNetComponent = new AdaNetComponent(3, 20);
        assertEquals("FullyConnected(units=20)", adaNetComponent.getEmadl().get(2));
    }

    public void testGetEmadlFirstLayer() {
        AdaNetComponent adaNetComponent = new AdaNetComponent(3, 20);
        assertEquals("FullyConnected(units=20) ->", adaNetComponent.getEmadl().get(0));
    }
}