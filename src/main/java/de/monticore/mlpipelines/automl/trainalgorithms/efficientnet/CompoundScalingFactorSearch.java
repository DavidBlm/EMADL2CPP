package de.monticore.mlpipelines.automl.trainalgorithms.efficientnet;

import de.monticore.lang.monticar.cnnarch._symboltable.ArchitectureSymbol;
import de.monticore.mlpipelines.automl.configuration.EfficientNetConfig;
import de.monticore.mlpipelines.pipelines.Pipeline;

public class CompoundScalingFactorSearch {
    private final NetworkScaler networkScaler;
    private final ArchitectureSymbol architecture;
    private final Pipeline trainPipeline;
    private final EfficientNetConfig config;
    private double bestAccuracy = 0.0;

    public CompoundScalingFactorSearch(
            ArchitectureSymbol architecture,
            EfficientNetConfig config,
            Pipeline networkTrainer, NetworkScaler networkScaler) {
        this.architecture = architecture;
        this.config = config;
        this.trainPipeline = networkTrainer;
        this.networkScaler = networkScaler;
    }

    public int findBestCompoundFactor(ScalingFactors bestScalingFactors) {
        int optimalPhi = 1;

        for (int currentPhi = 1; currentPhi <= this.config.getPhi(); currentPhi++) {
            System.out.println();
            System.out.println("Current Phi: " + currentPhi);
            this.networkScaler.scale(this.architecture, bestScalingFactors, currentPhi);
            trainPipeline.execute();
            this.networkScaler.rollbackScaledNetwork();
            float newAccuracy = trainPipeline.getTrainedAccuracy();
            if (trainPipeline.getTrainedAccuracy() > bestAccuracy) {
                bestAccuracy = newAccuracy;
                optimalPhi = currentPhi;
            }
        }

        return optimalPhi;
    }
}
