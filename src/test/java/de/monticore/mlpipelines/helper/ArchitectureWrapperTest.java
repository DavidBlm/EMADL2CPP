package de.monticore.mlpipelines.helper;

import de.monticore.lang.monticar.cnnarch._symboltable.ArchitectureSymbol;
import junit.framework.TestCase;

public class ArchitectureWrapperTest extends TestCase {

    public void testConstructor() {
        ArchitectureWrapper architectureWrapper = new ArchitectureWrapper(null);
        assertNotNull(architectureWrapper);
    }

    public void testGetArchitecture() {
        ArchitectureSymbol architecture = new ArchitectureSymbol();
        ArchitectureWrapper architectureWrapper = new ArchitectureWrapper(architecture);
        assertEquals(architecture, architectureWrapper.getArchitecture());
    }

    public void testSetArchitecture() {
        ArchitectureSymbol architecture = new ArchitectureSymbol();
        ArchitectureWrapper architectureWrapper = new ArchitectureWrapper(architecture);
        ArchitectureSymbol architecture2 = new ArchitectureSymbol();
        architectureWrapper.setArchitecture(architecture2);
        assertEquals(architecture2, architectureWrapper.getArchitecture());
    }

    public void testTestClone() {
        ArchitectureSymbol architecture = new ArchitectureSymbol();
        ArchitectureWrapper architectureWrapper = new ArchitectureWrapper(architecture);
        ArchitectureWrapper architectureWrapper2 = architectureWrapper.clone();
        ArchitectureSymbol clonedArchitecture = architectureWrapper2.getArchitecture();
        assertNotSame(architectureWrapper, architectureWrapper2);
        assertEquals(architecture, clonedArchitecture);
        assertNotSame(architecture, clonedArchitecture);
    }
}