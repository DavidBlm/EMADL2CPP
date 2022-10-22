package de.monticore.mlpipelines.automl.trainalgorithms.adanet;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;

public class AdaNetCandidateTest extends TestCase {

    public void testConstructor() {
        AdaNetCandidate adanetCandidate = new AdaNetCandidate();
        assertNotNull(adanetCandidate);
    }

    public void testConstructorWithParameters() {
        AdaNetComponent component = new AdaNetComponent(1);
        AdaNetCandidate adanetCandidate = new AdaNetCandidate(component, null);
        assertNotNull(adanetCandidate);
    }

    public void testConstructorCreatesEmptyPreviousComponents() {
        AdaNetComponent component = new AdaNetComponent(1);
        AdaNetCandidate adanetCandidate = new AdaNetCandidate(component, null);
        assertNotNull(adanetCandidate.getPreviousComponents());
        assertEquals(0, adanetCandidate.getPreviousComponents().size());
    }

    public void testGetComponent() {
        AdaNetComponent component = new AdaNetComponent(1);
        AdaNetCandidate adanetCandidate = new AdaNetCandidate(component, null);
        assertEquals(component, adanetCandidate.getComponent());
    }

    public void testSetComponent() {
        AdaNetComponent component = new AdaNetComponent(1);
        AdaNetCandidate adanetCandidate = new AdaNetCandidate();
        adanetCandidate.setComponent(component);
        assertEquals(component, adanetCandidate.getComponent());
    }

    public void testGetPreviousComponents() {
        AdaNetComponent component = new AdaNetComponent(1);
        List<AdaNetComponent> previousComponents = new ArrayList<>();
        AdaNetCandidate adanetCandidate = new AdaNetCandidate(component, previousComponents);
        assertEquals(previousComponents, adanetCandidate.getPreviousComponents());
    }

    public void testSetPreviousComponents() {
        AdaNetComponent component = new AdaNetComponent(1);
        List<AdaNetComponent> previousComponents = new ArrayList<>();
        AdaNetCandidate adanetCandidate = new AdaNetCandidate(component, null);
        adanetCandidate.setPreviousComponents(previousComponents);
        assertEquals(previousComponents, adanetCandidate.getPreviousComponents());
    }
}