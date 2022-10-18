package de.monticore.mlpipelines.automl.trainalgorithms.efficientnet;

import de.monticore.lang.monticar.cnnarch._symboltable.ArchitectureSymbol;
import de.monticore.mlpipelines.Pipeline;
import de.monticore.mlpipelines.automl.configuration.Configuration;
import de.monticore.mlpipelines.automl.configuration.EfficientNetConfig;
import de.monticore.mlpipelines.automl.configuration.TrainAlgorithmConfig;

public class ScalingFactorsGridSearch {

    private final double bestAccuracy = 0;
    private final NetworkScaler networkScaler;
    private final ArchitectureSymbol architecture;
    private final Pipeline trainPipeline;
    private final EfficientNetConfig trainConfig;
    private ScalingFactors bestScalingFactors;
    private double locBestAccuracy;

    public ScalingFactorsGridSearch(ArchitectureSymbol architecture,
                                    EfficientNetConfig trainConfig,
                                    Pipeline networkTrainer,
                                    NetworkScaler networkScaler) {
        this.architecture = architecture;
        this.trainConfig = trainConfig;
        this.trainPipeline = networkTrainer;
        this.networkScaler = networkScaler;
    }

    public ScalingFactors findScalingFactors() {
        initVariables();

        for (double gamma = trainConfig.getMinScalingFactors().gamma;
             gamma <= trainConfig.getMaxScalingFactors().gamma;
             gamma += trainConfig.getScalingFactorsStepSize().gamma) {

            for (double beta = trainConfig.getMinScalingFactors().beta;
                 beta <= trainConfig.getMaxScalingFactors().beta;
                 beta += trainConfig.getScalingFactorsStepSize().beta) {

                float alpha = alphaFromFlopsCondition(beta, gamma);
                this.checkScalingFactors(new ScalingFactors(alpha, beta, gamma));
            }
        }

        return bestScalingFactors;
    }

    private float alphaFromFlopsCondition(double beta, double gamma) {
        float foundAlpha = (float) (trainConfig.getFlopsConditionValue() / Math.pow(beta * gamma, 2));
        return foundAlpha;
    }

    private void checkScalingFactors(ScalingFactors scalingFactors) {
        if (scalingFactors.alpha < trainConfig.getMinScalingFactors().alpha) {
            return;
        }

        int phi = 1;
        ArchitectureSymbol scaledArchitecture = this.networkScaler.scale(this.architecture, scalingFactors, phi);
        Configuration pipelineConfig = getConfigurationFromTrainConfig(this.trainConfig);
        trainPipeline.train(scaledArchitecture, pipelineConfig);
        checkIfScalingFactorsAreBetterThanBest(scalingFactors);
    }

    private void checkIfScalingFactorsAreBetterThanBest(ScalingFactors scalingFactors) {
        if (trainPipeline.getTrainedAccuracy() > locBestAccuracy) {
            locBestAccuracy = trainPipeline.getTrainedAccuracy();
            bestScalingFactors = scalingFactors;
        }
    }

    private void initVariables() {
        this.bestScalingFactors = new ScalingFactors(1, 1, 1);
        this.locBestAccuracy = 0;
    }

    private Configuration getConfigurationFromTrainConfig(TrainAlgorithmConfig trainConfig) {
        Configuration config = new Configuration();
        config.setTrainAlgorithmConfig(trainConfig);
        return config;
    }

}
