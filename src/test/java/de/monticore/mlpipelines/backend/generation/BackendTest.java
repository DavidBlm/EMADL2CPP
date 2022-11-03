package de.monticore.mlpipelines.backend.generation;

import de.monticore.mlpipelines.configuration.ExperimentConfiguration;
import de.monticore.mlpipelines.configuration.MontiAnnaContext;

class BackendTest {

    protected static MontiAnnaContext initialiseContext() {
        final ExperimentConfiguration experimentConfiguration = new ExperimentConfiguration("", "", "", "./target/generated-sources-emadl");
        MontiAnnaContext.getInstance().initContext("src/test/resources/models/", "mnist.MnistClassifier", experimentConfiguration);
        return MontiAnnaContext.getInstance();
    }
}