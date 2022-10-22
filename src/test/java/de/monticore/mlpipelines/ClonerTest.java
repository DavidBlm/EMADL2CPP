package de.monticore.mlpipelines;

import com.rits.cloning.Cloner;
import de.monticore.lang.monticar.cnnarch._symboltable.ArchitectureSymbol;
import junit.framework.TestCase;

public class ClonerTest extends TestCase {
    public void testConstructor() {
        Cloner cloner = new Cloner();
        assertNotNull(cloner);
    }

    public void testDeepClone() {
        Cloner cloner = new Cloner();
        Object object = new Object();
        Object clone = cloner.deepClone(object);
        assertNotNull(clone);
        assertNotSame(object, clone);
    }

    public void testDeepCloneWithArchitectureSymbol() {
        Cloner cloner = new Cloner();
        ArchitectureSymbol architectureSymbol = new ArchitectureSymbol();
        ArchitectureSymbol clone = cloner.deepClone(architectureSymbol);
        assertNotNull(clone);
        assertNotSame(architectureSymbol, clone);
    }

    public void testShallowClone() {
        Cloner cloner = new Cloner();
        Object object = new Object();
        Object clone = cloner.shallowClone(object);
        assertNotNull(clone);
        assertNotSame(object, clone);
    }
}
