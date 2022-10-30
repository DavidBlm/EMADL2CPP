package de.monticore.mlpipelines.workflow;

import de.monticore.mlpipelines.configuration.MontiAnnaContext;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertTrue;

class BackendTest {

    @Test
    void generateBackendArtefacts() {
        MontiAnnaContext.getInstance().initContext("src/test/resources/models/customMNISTCalculator", "cNNCalculator.Connector");
        new DummyWorkflow().generateBackendArtefacts("src/test/resources/experiment/pytorch");
        assertTrue(new File("src/test/resources/experiment/pytorch/CNNNet_LeNet.py").exists());
    }
}