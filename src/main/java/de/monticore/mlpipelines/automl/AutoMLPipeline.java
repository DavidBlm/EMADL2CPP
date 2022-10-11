package de.monticore.mlpipelines.automl;

import de.monticore.lang.embeddedmontiarc.embeddedmontiarc._symboltable.instanceStructure.EMAComponentInstanceSymbol;
import de.monticore.lang.monticar.cnnarch._symboltable.ArchitectureSymbol;
import de.monticore.mlpipelines.Pipeline;
import de.monticore.mlpipelines.automl.configuration.Configuration;
import de.monticore.mlpipelines.automl.trainalgorithms.TrainAlgorithm;
import de.monticore.mlpipelines.automl.trainalgorithms.TrainAlgorithmBuilder;
import de.monticore.mlpipelines.parser.ConfFile2ConfigurationParser;
import de.monticore.symboltable.Scope;

import java.nio.file.Path;
import java.nio.file.Paths;

import static de.monticore.lang.monticar.emadl.generator.EMADLAbstractSymtab.createSymTab;

public class AutoMLPipeline extends Pipeline {
    private Configuration configuration;
    private Pipeline trainPipeline;
    private ArchitectureSymbol architecture;
    private TrainAlgorithm trainAlgorithm;
    private String resourcePath = "src/main/resources/";
    private TrainAlgorithmBuilder trainAlgorithmBuilder;

    public AutoMLPipeline() {
        trainAlgorithmBuilder = new TrainAlgorithmBuilder();
    }

    @Override
    public void train(ArchitectureSymbol architecture, Configuration configuration) {
        this.architecture = architecture;
        this.configuration = configuration;

        loadTrainAlgorithm();
        trainAlgorithm.train(architecture);
    }

    public void train(ArchitectureSymbol architecture, String modelName) {
        Configuration config = loadConfig(modelName);
        this.train(architecture, config);
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public Pipeline getTrainPipeline() {
        return trainPipeline;
    }

    public TrainAlgorithm getTrainAlgorithm() {
        return trainAlgorithm;
    }

    public ArchitectureSymbol getArchitecture() {
        return architecture;
    }

    public void setResourcePath(String resourcePath) {
        this.resourcePath = resourcePath;
    }

    public TrainAlgorithmBuilder getTrainAlgorithmBuilder() {
        return trainAlgorithmBuilder;
    }

    public void setTrainAlgorithmBuilder(TrainAlgorithmBuilder trainAlgorithmBuilder) {
        this.trainAlgorithmBuilder = trainAlgorithmBuilder;
    }

    private Configuration loadConfig(String modelName) {
        // Get model path and model name from configurationPath
        Path resourcesPath = Paths.get(this.resourcePath);
        ConfFile2ConfigurationParser parser = new ConfFile2ConfigurationParser();
        return parser.getConfiguration(resourcesPath, modelName);
    }

    public void loadTrainAlgorithm() {
        trainAlgorithmBuilder.setConfig(configuration.getTrainAlgorithmConfig());
        this.trainAlgorithm = trainAlgorithmBuilder.build();
    }

    private void loadArchitecture() {
        Scope symTab = createSymTab("src/test/resources/models");
        EMAComponentInstanceSymbol c = symTab.<EMAComponentInstanceSymbol>resolve("efficientNetB0",
                EMAComponentInstanceSymbol.KIND).orElse(null);
        ArchitectureSymbol resolvedArchitecture = c.getSpannedScope().<ArchitectureSymbol>resolve("", ArchitectureSymbol.KIND).get();
        this.architecture = resolvedArchitecture;
    }
}
