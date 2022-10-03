package de.monticore.mlpipelines.automl.trainalgorithms;

import de.monticore.mlpipelines.automl.configuration.TrainAlgorithmConfig;
import de.monticore.mlpipelines.automl.trainalgorithms.efficientnet.EfficientNet;
import de.monticore.mlpipelines.automl.trainalgorithms.adanet.AdaNet;


public class TrainAlgorithmBuilder {
    private final String EfficientNet = "EfficientNet";
    private final String AdaNet = "AdaNet";

    private TrainAlgorithmConfig config;

    public void setConfig(TrainAlgorithmConfig config) {
        this.config = config;
    }

    public TrainAlgorithm build() {
        TrainAlgorithm trainAlgorithm = getTrainAlgorithm();
        trainAlgorithm.setTrainConfiguration(config);
        return trainAlgorithm;
    }

    private TrainAlgorithm getTrainAlgorithm() {
        switch (config.getTrainAlgorithmName()) {
            case EfficientNet:
                return new EfficientNet();
            case AdaNet:
                return new AdaNet();
            default:
                throw new IllegalArgumentException("Train algorithm " + config.getTrainAlgorithmName() + " not supported");
        }
    }
}
