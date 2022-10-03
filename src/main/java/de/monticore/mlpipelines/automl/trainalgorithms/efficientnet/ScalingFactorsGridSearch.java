package de.monticore.mlpipelines.automl.trainalgorithms.efficientnet;

import de.monticore.lang.monticar.cnnarch._symboltable.ArchitectureSymbol;
import de.monticore.mlpipelines.automl.trainalgorithms.TrainAlgorithm;
import de.monticore.mlpipelines.automl.configuration.EfficientNetConfig;

public class ScalingFactorsGridSearch{

    private double bestAccuracy = 0;
    private ScalingFactors bestScalingFactors = null;
    private ArchitectureSymbol architecture;
    private TrainAlgorithm networkTrainer;
    private EfficientNetConfig trainConfig;

    public ScalingFactorsGridSearch(ArchitectureSymbol architecture,
                                    EfficientNetConfig trainConfig,
                                    TrainAlgorithm networkTrainer
                                    //NetworkScaler,
    )
    {
        this.architecture = architecture;
        this.trainConfig = trainConfig;
        this.networkTrainer = networkTrainer;
    }

    private double alphaFromFlopsCondition(double beta, double gamma){
        double foundAlpha;
        foundAlpha = EfficientNetConfig.FLOPS_CONDITION_VALUE / Math.pow(beta*gamma,2);
        return foundAlpha;
    }

    public ScalingFactors findScalingFactors(){
        ScalingFactors bestScalingFactors = new ScalingFactors(1,1,1);
        double locBestAccuracy = 0;
        double locAlpha = 1;
        double locBeta = 1;
        double locGamma;
        EfficientNetConfig locTrainConfig = trainConfig;

        for(locGamma = EfficientNetConfig.MIN_GAMMA; locGamma <= EfficientNetConfig.MAX_GAMMA; locGamma +=EfficientNetConfig.GAMMA_STEP_SIZE){
            for(locBeta = EfficientNetConfig.MIN_BETA; locBeta <= EfficientNetConfig.MAX_BETA; locBeta += EfficientNetConfig.BETA_STEP_SIZE){
                locAlpha = alphaFromFlopsCondition(locBeta, locGamma);

                if(locAlpha < EfficientNetConfig.MIN_ALPHA){
                    break;
                }
                else
                {
                    //scaleNetwork()
                    locTrainConfig.alpha = locAlpha;
                    locTrainConfig.beta = locBeta;
                    locTrainConfig.gamma = locGamma;
                    networkTrainer.train(architecture, locTrainConfig);
                    if(networkTrainer.trainedAccuracy > locBestAccuracy){
                        locBestAccuracy = networkTrainer.trainedAccuracy;
                    }
                }
            }
        }

        bestScalingFactors.alpha= locAlpha;
        bestScalingFactors.beta= locBeta;
        bestScalingFactors.gamma= locGamma;

        return bestScalingFactors;
    }

}
