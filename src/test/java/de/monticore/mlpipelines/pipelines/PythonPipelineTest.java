package de.monticore.mlpipelines.pipelines;

import de.monticore.mlpipelines.configuration.ExperimentConfiguration;
import de.monticore.mlpipelines.configuration.MontiAnnaContext;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PythonPipelineTest {

    @Test
    void runScript() throws InterruptedException, IOException {
        final ExperimentConfiguration experimentConfiguration = new ExperimentConfiguration("", "",
                "target/generated-sources/", "");
        MontiAnnaContext.getInstance().initContext(Paths.get(""), "", experimentConfiguration);
        final Process process = new PythonPipeline(null).runScript();
        String result = IOUtils.toString(process.getInputStream(), StandardCharsets.UTF_8);
        assertEquals("Can be executed", result);
    }

}