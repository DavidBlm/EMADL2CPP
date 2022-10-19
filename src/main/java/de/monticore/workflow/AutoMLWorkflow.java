package de.monticore.workflow;

import de.monticore.lang.monticar.cnnarch._symboltable.ArchitectureSymbol;
import de.monticore.mlpipelines.automl.AutoMLPipeline;
import de.monticore.mlpipelines.automl.configuration.Configuration;
import de.monticore.mlpipelines.automl.trainalgorithms.TrainAlgorithmBuilder;
import de.monticore.mlpipelines.parser.ConfFile2ConfigurationParser;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

public class AutoMLWorkflow {
    private String resourcePath = "src/main/resources/";
    private ArchitectureSymbol architecture;
    private Configuration configuration;
    private ConfFile2ConfigurationParser confFile2ConfigurationParser;

    public void execute(ArchitectureSymbol architecture, Map<String,String> confScmMap) {
        this.architecture = architecture;
        this.configuration = loadConfig(confScmMap);
        TrainAlgorithmBuilder trainAlgorithmBuilder = new TrainAlgorithmBuilder();
        AutoMLPipeline pipeline = new AutoMLPipeline();
        pipeline.setTrainAlgorithmBuilder(trainAlgorithmBuilder);
        pipeline.train(architecture, configuration);
    }

    private Configuration loadConfig(Map<String,String> modelNames) {
        Path resourcesPath = Paths.get(this.resourcePath + "Configuration");
        Configuration configuration = this.confFile2ConfigurationParser.getConfiguration(resourcesPath, modelNames);
        return configuration;
    }

    public void setResourcePath(String resourcePath) {
        this.resourcePath = resourcePath;
    }

    public String getResourcePath() {
        return this.resourcePath;
    }

    public ArchitectureSymbol getArchitecture() {
        return architecture;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public ConfFile2ConfigurationParser getConfFile2ConfigurationParser() {
        return confFile2ConfigurationParser;
    }

    public void setConfFile2ConfigurationParser(ConfFile2ConfigurationParser confFile2ConfigurationParser) {
        this.confFile2ConfigurationParser = confFile2ConfigurationParser;
    }
}
