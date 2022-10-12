package de.monticore.mlpipelines.automl;

import de.monticore.lang.embeddedmontiarc.embeddedmontiarc._symboltable.instanceStructure.EMAComponentInstanceSymbol;
import de.monticore.lang.monticar.cnnarch._symboltable.ArchitectureSymbol;
import de.monticore.mlpipelines.Pipeline;
import de.monticore.mlpipelines.automl.configuration.Configuration;
import de.monticore.mlpipelines.automl.trainalgorithms.TrainAlgorithm;
import de.monticore.mlpipelines.automl.trainalgorithms.TrainAlgorithmBuilder;
import de.monticore.symboltable.Scope;

import static de.monticore.lang.monticar.emadl.generator.EMADLAbstractSymtab.createSymTab;

public class AutoMLPipeline extends Pipeline {
    private Configuration configuration;
    private Pipeline trainPipeline;
    private ArchitectureSymbol architecture;
    private TrainAlgorithm trainAlgorithm;
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

    public void loadTrainAlgorithm() {
        trainAlgorithmBuilder.setConfig(configuration.getTrainAlgorithmConfig());
        this.trainAlgorithm = trainAlgorithmBuilder.build();
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

    public TrainAlgorithmBuilder getTrainAlgorithmBuilder() {
        return trainAlgorithmBuilder;
    }

    public void setTrainAlgorithmBuilder(TrainAlgorithmBuilder trainAlgorithmBuilder) {
        this.trainAlgorithmBuilder = trainAlgorithmBuilder;
    }
}
