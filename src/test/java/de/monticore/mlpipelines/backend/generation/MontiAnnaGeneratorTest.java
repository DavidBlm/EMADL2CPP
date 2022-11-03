package de.monticore.mlpipelines.backend.generation;

import de.monticore.mlpipelines.configuration.MontiAnnaContext;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.util.Arrays;

import static de.monticore.mlpipelines.util.TestUtil.checkFilesAreEqual;
import static de.monticore.mlpipelines.util.TestUtil.checkFindingsCount;


class MontiAnnaGeneratorTest extends BackendTest {

    @Test
    void backendGenerationWithEMADLGenerator() {
        final MontiAnnaContext montiAnnaContext = initialiseContext();
        new MontiAnnaGenerator(montiAnnaContext).generateTargetBackendArtefacts();
        checkFindingsCount();

        checkFilesAreEqual(
                Paths.get(montiAnnaContext.getExperimentConfiguration().getGenerationTargetPath()),
                Paths.get("./src/test/resources/target_code/pytorch/mnist"),
                Arrays.asList(
                        "mnist_mnistClassifier.cpp",
                        "mnist_mnistClassifier.h",
                        "CNNNet_mnist_mnistClassifier_net.py",
                        "mnist_mnistClassifier_net.h",
                        "CNNTranslator.h",
                        "mnist_mnistClassifier_calculateClass.h",
                        "execute_mnist_mnistClassifier_net",
                        "CNNPredictor_mnist_mnistClassifier_net.h"));
    }

}