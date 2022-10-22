package de.monticore.mlpipelines.automl.trainalgorithms;

import de.monticore.lang.monticar.cnnarch._symboltable.ArchitectureSymbol;
import de.monticore.mlpipelines.Pipeline;
import de.monticore.mlpipelines.automl.configuration.TrainAlgorithmConfig;

public abstract class TrainAlgorithm {
    public double trainedAccuracy;
    private TrainAlgorithmConfig trainConfiguration;
    private Pipeline trainPipeline;
    private ArchitectureSymbol startNetwork;

    public abstract void execute(ArchitectureSymbol startNetwork);

    public <T extends TrainAlgorithmConfig> T getTrainConfiguration() {
        return (T) trainConfiguration;
    }

    public void setTrainConfiguration(TrainAlgorithmConfig trainConfiguration) {
        this.trainConfiguration = trainConfiguration;
    }

    public void setTrainPipeline(Pipeline trainPipeline) {
        this.trainPipeline = trainPipeline;
    }

    public void setStartNetwork(ArchitectureSymbol startNetwork) {
        this.startNetwork = startNetwork;
    }

    public Pipeline getTrainPipeline() {
        return trainPipeline;
    }

    public ArchitectureSymbol getStartNetwork() {
        return startNetwork;
    }
}
