package de.monticore.mlpipelines.automl;

import de.monticore.lang.monticar.cnnarch._symboltable.ArchitectureSymbol;
import de.monticore.mlpipelines.Pipeline;
import de.monticore.mlpipelines.automl.configuration.TrainAlgorithmConfig;

public abstract class AutoMLTrainAlgorithm {
    public double trainedAccuracy;
    private TrainAlgorithmConfig trainConfiguration;
    private Pipeline trainPipeline;
    private ArchitectureSymbol startNetwork;

    public abstract void train(ArchitectureSymbol startNetwork, TrainAlgorithmConfig trainConfiguration);
}
