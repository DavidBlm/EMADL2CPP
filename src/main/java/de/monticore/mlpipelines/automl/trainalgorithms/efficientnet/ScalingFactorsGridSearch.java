package de.monticore.mlpipelines.automl.trainalgorithms.efficientnet;

import de.monticore.lang.monticar.cnnarch._symboltable.ArchitectureSymbol;
import de.monticore.mlpipelines.Pipeline;
import de.monticore.mlpipelines.automl.configuration.Configuration;
import de.monticore.mlpipelines.automl.trainalgorithms.TrainAlgorithm;
import de.monticore.mlpipelines.automl.configuration.EfficientNetConfig;

public class ScalingFactorsGridSearch{

    private double bestAccuracy = 0;
    private ScalingFactors bestScalingFactors = null;
    private ArchitectureSymbol architecture;
    private Pipeline trainPipeline;
    private Configuration trainConfig;

    public ScalingFactorsGridSearch(ArchitectureSymbol architecture,
                                    Configuration trainConfig,
                                    Pipeline networkTrainer
                                    //NetworkScaler,
    )
    {
        this.architecture = architecture;
        this.trainConfig = trainConfig;
        this.trainPipeline = networkTrainer;
    }

    private float alphaFromFlopsCondition(double beta, double gamma){
        float foundAlpha = (float)(EfficientNetConfig.FLOPS_CONDITION_VALUE / Math.pow(beta*gamma,2));
        return foundAlpha;
    }

    public ScalingFactors findScalingFactors(){
        ScalingFactors bestScalingFactors = new ScalingFactors(1,1,1);
        ScalingFactors currentScalingFactors = new ScalingFactors(1,1,1);
        double locBestAccuracy = 0;

        for(float locGamma = EfficientNetConfig.MIN_SCALING_FACTORS.gamma;
            locGamma <= EfficientNetConfig.MAX_SCALING_FACTORS.gamma;
            locGamma +=EfficientNetConfig.SCALING_FACTORS_STEP_SIZE.gamma){
            for(float locBeta = EfficientNetConfig.MIN_SCALING_FACTORS.beta;
                locBeta <= EfficientNetConfig.MAX_SCALING_FACTORS.beta;
                locBeta += EfficientNetConfig.SCALING_FACTORS_STEP_SIZE.beta){
                float locAlpha = alphaFromFlopsCondition(locBeta, locGamma);

                if(currentScalingFactors.alpha < EfficientNetConfig.MIN_SCALING_FACTORS.alpha){
                    break;
                }
                else
                {
                    //scaleNetwork()
                    currentScalingFactors = new ScalingFactors(locAlpha, locBeta, locGamma);
                    trainPipeline.train(architecture, this.trainConfig);
                    if(trainPipeline.getTrainedAccuracy() > locBestAccuracy){
                        locBestAccuracy = trainPipeline.getTrainedAccuracy();
                        bestScalingFactors = currentScalingFactors;
                    }
                }
            }
        }

        return bestScalingFactors;
    }

}
