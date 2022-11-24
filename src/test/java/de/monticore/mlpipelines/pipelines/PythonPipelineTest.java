package de.monticore.mlpipelines.pipelines;

import de.monticore.lang.monticar.cnnarch.generator.training.LearningMethod;
import de.monticore.mlpipelines.configuration.ExperimentConfiguration;
import de.monticore.mlpipelines.configuration.MontiAnnaContext;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class PythonPipelineTest {

    @Test
    void runScript() throws InterruptedException, IOException {
        final ExperimentConfiguration experimentConfiguration = new ExperimentConfiguration("", "", "src/test/resources/experiment", "");
        MontiAnnaContext.getInstance().initContext("", "", experimentConfiguration);
        final Process process = new PythonPipeline(null).runScript();
        String result = IOUtils.toString(process.getInputStream(), StandardCharsets.UTF_8);
        assertEquals("Can be executed", result);
    }
}