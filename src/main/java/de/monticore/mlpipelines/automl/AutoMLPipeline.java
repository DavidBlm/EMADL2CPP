package de.monticore.mlpipelines.automl;

import de.monticore.lang.embeddedmontiarc.embeddedmontiarc._symboltable.instanceStructure.EMAComponentInstanceSymbol;
import de.monticore.lang.monticar.cnnarch._symboltable.ArchitectureSymbol;
import de.monticore.mlpipelines.Pipeline;
import de.monticore.mlpipelines.automl.configuration.Configuration;
import de.monticore.mlpipelines.automl.configuration.TrainAlgorithmConfig;
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

    public void train(ArchitectureSymbol architecture, Configuration configuration) {
        this.configuration = configuration;
        this.architecture = architecture;

        loadConfig();
        loadArchitecture();
        loadTrainAlgorithm();
        trainAlgorithm.train(architecture);
    }

    public void loadTrainAlgorithm() {
        TrainAlgorithmBuilder builder = new TrainAlgorithmBuilder();
        builder.setConfig(configuration.getTrainAlgorithmConfig());
        this.trainAlgorithm = builder.build();
    }

    private void loadConfig() {
        // Example usage of ConfFile2ConfigurationParser to get a Configuration object
        String modelName = "AutoMLExample.conf";
        Path modelPath = Paths.get("src/main/resources");
        ConfFile2ConfigurationParser parser = new ConfFile2ConfigurationParser(modelPath, modelName);
        this.configuration = parser.getConfiguration();
    }

    private void loadArchitecture() {
        Scope symTab = createSymTab("src/test/resources/models");
        EMAComponentInstanceSymbol c = symTab.<EMAComponentInstanceSymbol>resolve("efficientNetB0",
                EMAComponentInstanceSymbol.KIND).orElse(null);
        ArchitectureSymbol resolvedArchitecture = c.getSpannedScope().<ArchitectureSymbol>resolve("", ArchitectureSymbol.KIND).get();
        this.architecture = resolvedArchitecture;
    }
}
