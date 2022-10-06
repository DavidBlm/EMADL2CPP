package de.monticore.mlpipelines;

import de.monticore.lang.monticar.cnnarch._symboltable.ArchitectureSymbol;
import de.monticore.mlpipelines.automl.configuration.Configuration;
import de.monticore.mlpipelines.automl.configuration.TrainAlgorithmConfig;

public abstract class Pipeline {
    private float trainedAccuracy;

    public float getTrainedAccuracy() {
        return trainedAccuracy;
    }

    public void setTrainedAccuracy(float trainedAccuracy) {
        this.trainedAccuracy = trainedAccuracy;
    }

    public abstract void train(ArchitectureSymbol architecture, String configurationPath);
    public abstract void train(ArchitectureSymbol architecture, Configuration configuration);
}
