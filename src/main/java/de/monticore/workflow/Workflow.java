package de.monticore.workflow;

import de.monticore.lang.embeddedmontiarc.embeddedmontiarc._symboltable.instanceStructure.EMAComponentInstanceSymbol;
import de.monticore.lang.monticar.cnnarch._symboltable.ArchitectureSymbol;
import de.monticore.mlpipelines.automl.AutoMLPipeline;
import de.monticore.mlpipelines.automl.configuration.Configuration;
import de.monticore.mlpipelines.automl.trainalgorithms.TrainAlgorithmBuilder;
import de.monticore.mlpipelines.parser.ConfFile2ConfigurationParser;
import de.monticore.symboltable.Scope;

import java.nio.file.Path;
import java.nio.file.Paths;

import static de.monticore.lang.monticar.emadl.generator.EMADLAbstractSymtab.createSymTab;

public class Workflow {
    private final String EFFICIENT_NET_B0 = "efficientNetB0";
    private String resourcePath = "src/main/resources/";
    private ArchitectureSymbol architecture;
    private Configuration configuration;
    private ConfFile2ConfigurationParser confFile2ConfigurationParser;

    public void execute(ArchitectureSymbol architecture, String configFileName) {
        this.architecture = architecture;
        this.configuration = loadConfig(configFileName);
        TrainAlgorithmBuilder trainAlgorithmBuilder = new TrainAlgorithmBuilder();
        AutoMLPipeline pipeline = new AutoMLPipeline();
        pipeline.setTrainAlgorithmBuilder(trainAlgorithmBuilder);
        pipeline.train(architecture, configuration);
    }

    private Configuration loadConfig(String modelName) {
        Path resourcesPath = Paths.get(this.resourcePath);
        if(this.confFile2ConfigurationParser == null) {
            throw new RuntimeException("confFile2ConfigurationParser is null");
        }
        Configuration configuration = this.confFile2ConfigurationParser.getConfiguration(resourcesPath, modelName);
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
