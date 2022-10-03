package de.monticore.mlpipelines.automl.efficientnet;

import de.monticore.lang.monticar.cnnarch._symboltable.ArchitectureSymbol;
import de.monticore.mlpipelines.automl.AutoMLTrainAlgorithm;
import de.monticore.mlpipelines.automl.configuration.EfficientNetConfig;

public class ScalingFactorsGridSearch{

    private double bestAccuracy = 0;
    private double alpha, beta, gamma = 0;
    private ScalingFactors bestScalingFactors = null;
    private ArchitectureSymbol architecture = null;
    private AutoMLTrainAlgorithm networkTrainer = null;
    private EfficientNetConfig trainConfig = null;

    public ScalingFactorsGridSearch(ArchitectureSymbol architecture,
                                    EfficientNetConfig trainConfig,
                                    AutoMLTrainAlgorithm networkTrainer
                                    //NetworkScaler,
    )
    {
        this.architecture = architecture;
        this.trainConfig = trainConfig;
        this.networkTrainer = networkTrainer;
    }

    private double alphaFromFlopsCondition(double beta, double gamma){
        double foundAlpha = 0;
        foundAlpha = trainConfig.FLOPS_CONDITION_VALUE / Math.pow(beta*gamma,2);
        return foundAlpha;
    }

    public ScalingFactors findScalingFactors(){
        ScalingFactors bestScalingFactors = null;
        double locBestAccuracy = 0;
        double locAlpha = 1, locBeta = 1, locGamma = 1;
        EfficientNetConfig locTrainConfig = trainConfig;

        for(locGamma = trainConfig.MIN_GAMMA; locGamma <= trainConfig.MAX_GAMMA; locGamma +=trainConfig.GAMMA_STEP_SIZE){
            for(locBeta = trainConfig.MIN_BETA; locBeta <= trainConfig.MAX_BETA; locBeta += trainConfig.BETA_STEP_SIZE){
                locAlpha = alphaFromFlopsCondition(beta, gamma);

                if(locAlpha < trainConfig.MIN_ALPHA){
                    break;
                }
                else{
                    //scaleNetwork()
                    locTrainConfig.alpha = locAlpha;
                    locTrainConfig.beta = locBeta;
                    locTrainConfig.gamma = locGamma;
                    networkTrainer.train(architecture, locTrainConfig);
                    if(networkTrainer.trainedAccuracy > bestAccuracy);
                    locBestAccuracy = networkTrainer.trainedAccuracy;
                }
            }
        }

        bestScalingFactors.alpha= locAlpha;
        bestScalingFactors.beta= locBeta;
        bestScalingFactors.gamma= locGamma;

        return bestScalingFactors;
    }

}
