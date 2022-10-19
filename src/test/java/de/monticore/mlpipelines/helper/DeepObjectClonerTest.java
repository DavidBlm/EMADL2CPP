package de.monticore.mlpipelines.helper;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;

public class DeepObjectClonerTest extends TestCase {

    public void testConstructor() {
        DeepObjectCloner<DeepObjectClonerTest> deepObjectCloner = new DeepObjectCloner<>();
        assertNotNull(deepObjectCloner);
    }

    public void testTestCloneEquals() {
        DeepObjectCloner<List<String>> deepObjectCloner = new DeepObjectCloner<>();
        List<String> dummyData = createDummyData();
        List<String> clone = deepObjectCloner.clone(dummyData);
        assertEquals(dummyData, clone);
    }

    private List<String> createDummyData() {
        List<String> dummyData = new ArrayList<>();
        dummyData.add("test");
        dummyData.add("test2");
        return dummyData;
    }

    public void testTestCloneNotSame() {
        DeepObjectCloner<List<String>> deepObjectCloner = new DeepObjectCloner<>();
        List<String> dummyData = createDummyData();
        List<String> clone = deepObjectCloner.clone(dummyData);
        assertNotSame(dummyData, clone);
    }
}