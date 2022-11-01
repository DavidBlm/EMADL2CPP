package de.monticore.mlpipelines.workflow;

import de.monticore.mlpipelines.configuration.MontiAnnaContext;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertTrue;

class BackendTest {

    //TODO Need to be fixed
    @Test
    void generateBackendArtefacts() {
        MontiAnnaContext.getInstance().initContext("src/test/resources/models/", "", null);
        new DummyWorkflow().generateBackendArtefactsIntoExpirement("src/test/resources/experiment/pytorch");
        assertTrue(new File("src/test/resources/experiment/pytorch/CNNNet_LeNet.py").exists());
    }
}