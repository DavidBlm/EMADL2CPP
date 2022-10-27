package de.monticore.mlpipelines.automl.trainalgorithms.adanet;

import de.monticore.lang.monticar.cnnarch._symboltable.ArchitectureSymbol;
import de.monticore.mlpipelines.ModelLoader;
import junit.framework.TestCase;

public class AdaNetTest extends TestCase {
    public void testConstructor() {
        AdaNet adaNet = new AdaNet();
        assertNotNull(adaNet);
    }

    public void testExecuteThrowsExceptionWhenTrainPipelineNotSet() {
        AdaNet adanet = new AdaNet();
        ArchitectureSymbol architectureSymbol = ModelLoader.loadAdaNet();
        boolean exceptionThrown = false;
        try {
            adanet.execute(architectureSymbol);
            fail("Expected exception");
        } catch (IllegalStateException e) {
            exceptionThrown = true;
            assertEquals("Train pipeline not set", e.getMessage());
        }

        assertTrue(exceptionThrown);
    }
}