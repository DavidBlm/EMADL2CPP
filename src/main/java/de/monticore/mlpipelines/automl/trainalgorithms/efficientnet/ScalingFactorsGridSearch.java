package de.monticore.mlpipelines.automl.trainalgorithms.efficientnet;

import de.monticore.lang.monticar.cnnarch._symboltable.ArchitectureSymbol;
import de.monticore.mlpipelines.Pipeline;
import de.monticore.mlpipelines.automl.configuration.Configuration;
import de.monticore.mlpipelines.automl.configuration.EfficientNetConfig;

public class ScalingFactorsGridSearch {

    private double bestAccuracy = 0;
    private NetworkScaler networkScaler;
    private ArchitectureSymbol architecture;
    private Pipeline trainPipeline;
    private Configuration trainConfig;
    private ScalingFactors bestScalingFactors;
    private double locBestAccuracy;

    public ScalingFactorsGridSearch(ArchitectureSymbol architecture,
                                    Configuration trainConfig,
                                    Pipeline networkTrainer,
                                    NetworkScaler networkScaler) {
        this.architecture = architecture;
        this.trainConfig = trainConfig;
        this.trainPipeline = networkTrainer;
        this.networkScaler = networkScaler;
    }

    public ScalingFactors findScalingFactors() {
        initVariables();

        for (float gamma = EfficientNetConfig.MIN_SCALING_FACTORS.gamma;
             gamma <= EfficientNetConfig.MAX_SCALING_FACTORS.gamma;
             gamma += EfficientNetConfig.SCALING_FACTORS_STEP_SIZE.gamma) {

            for (float beta = EfficientNetConfig.MIN_SCALING_FACTORS.beta;
                 beta <= EfficientNetConfig.MAX_SCALING_FACTORS.beta;
                 beta += EfficientNetConfig.SCALING_FACTORS_STEP_SIZE.beta) {

                float alpha = alphaFromFlopsCondition(beta, gamma);
                this.checkScalingFactors(new ScalingFactors(alpha, beta, gamma));
            }
        }

        return bestScalingFactors;
    }

    private float alphaFromFlopsCondition(double beta, double gamma) {
        float foundAlpha = (float) (EfficientNetConfig.FLOPS_CONDITION_VALUE / Math.pow(beta * gamma, 2));
        return foundAlpha;
    }

    private void checkScalingFactors(ScalingFactors scalingFactors) {
        if (scalingFactors.alpha < EfficientNetConfig.MIN_SCALING_FACTORS.alpha) {
            return;
        }

        ArchitectureSymbol scaledArchitecture = this.networkScaler.scale(this.architecture, scalingFactors);
        trainPipeline.train(scaledArchitecture, this.trainConfig);
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

}
