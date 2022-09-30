package de.monticore.mlpipelines.automl;

import de.monticore.mlpipelines.Pipeline;
import de.monticore.mlpipelines.parser.ConfFile2ConfigurationParser;

import java.nio.file.Path;
import java.nio.file.Paths;

public class AutoMLPipeline extends Pipeline {
    private Configuration configuration;
    private Pipeline trainPipeline;

    public void train(){
        // Example usage of ConfFile2ConfigurationParser to get a Configuration object
        Path modelPath = Paths.get("src/main/resources");
        String modelName = "AutoMLExample.conf";
        ConfFile2ConfigurationParser parser = new ConfFile2ConfigurationParser(modelPath, modelName);
        this.configuration = parser.getConfiguration();
    }
}
