package de.monticore.mlpipelines.automl.trainalgorithms.efficientnet;

import de.monticore.lang.math._symboltable.expression.MathNumberExpressionSymbol;
import de.monticore.lang.monticar.cnnarch._symboltable.ArchitectureElementSymbol;
import de.monticore.lang.monticar.cnnarch._symboltable.ArchitectureSymbol;
import de.monticore.lang.monticar.cnnarch._symboltable.NetworkInstructionSymbol;
import de.monticore.lang.monticar.cnnarch._symboltable.SerialCompositeElementSymbol;
import de.monticore.mlpipelines.automl.configuration.EfficientNetConfig;
import de.monticore.mlpipelines.automl.helper.ArchitectureHelper;
import de.monticore.mlpipelines.automl.helper.MathNumberExpressionWrapper;
import de.monticore.mlpipelines.pipelines.Pipeline;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ScalingFactorsGridSearch {
    private final int standardPhi = 1;

    private final NetworkScaler networkScaler;
    private final ArchitectureSymbol architecture;
    private final Pipeline trainPipeline;
    List<OriginalLayerParams> parametersReference;
    private final EfficientNetConfig config;
    private ScalingFactors bestScalingFactors;
    private double bestAccuracy;

    public ScalingFactorsGridSearch(
            ArchitectureSymbol architecture,
            EfficientNetConfig config,
            Pipeline networkTrainer,
            NetworkScaler networkScaler) {
        this.architecture = architecture;
        this.config = config;
        this.trainPipeline = networkTrainer;
        this.networkScaler = networkScaler;
    }

    public ScalingFactors findBestScalingFactors() {
        initVariables();
        List<ScalingFactors> possibleScalingFactors = getPossibleScalingFactors();

        for (int i = 0; i < possibleScalingFactors.size(); i++) {
            ScalingFactors scalingFactors = possibleScalingFactors.get(i);
            System.out.println();
            System.out.println("Iteration " + (i + 1) + " of " + possibleScalingFactors.size());
            System.out.println("Test these scaling factors: " + scalingFactors.toString());
            checkScalingFactors(scalingFactors);
        }

        System.out.println();
        System.out.println("Best scaling factors: " + bestScalingFactors.toString());
        System.out.println("Best scalingFactors accuracy: " + bestAccuracy);
        return bestScalingFactors;
    }

    public List<ScalingFactors> getPossibleScalingFactors() {
        List<ScalingFactors> scalingFactors = new ArrayList<>();
        for (double gamma = config.getMinScalingFactors().gamma;
             gamma <= config.getMaxScalingFactors().gamma;
             gamma += config.getScalingFactorsStepSize().gamma) {

            for (double beta = config.getMinScalingFactors().beta;
                 beta <= config.getMaxScalingFactors().beta;
                 beta += config.getScalingFactorsStepSize().beta) {

                float alpha = alphaFromFlopsCondition(beta, gamma);
                scalingFactors.add(new ScalingFactors(alpha, beta, gamma));
            }
        }

        return scalingFactors;
    }

    private float alphaFromFlopsCondition(double beta, double gamma) {
        float foundAlpha = (float) (config.getFlopsConditionValue() / Math.pow(beta * gamma, 2));
        return foundAlpha;
    }

    private void checkScalingFactors(ScalingFactors scalingFactors) {
        if (scalingFactors.alpha < config.getMinScalingFactors().alpha) {
            return;
        }

        //this.networkScaler.parametersReference = parametersReference;
        this.networkScaler.scale(this.architecture, scalingFactors, this.standardPhi);
        trainPipeline.execute();
        rollbackScaledNetwork();
        checkIfScalingFactorsAreBetterThanBest(scalingFactors);
    }

    private void rollbackScaledNetwork(){
        NetworkInstructionSymbol networkInstruction = this.architecture.getNetworkInstructions().get(0);
        SerialCompositeElementSymbol networkInstructionBody = networkInstruction.getBody();
        List<ArchitectureElementSymbol> architectureElements = networkInstructionBody.getElements();
        int i=0;
        List<String> allowedLayers = Arrays.asList("residualBlock", "reductionBlock", "stem");
        for (ArchitectureElementSymbol architectureElement : architectureElements) {
            int channelsIndex=1;
            if (allowedLayers.contains(architectureElement.getName())){
                ArrayList symbolExpressions = ArchitectureHelper.getExpressions(architectureElement);

                if (architectureElement.getName().equals("residualBlock")){
                    channelsIndex = 5;
                    int depthIndex = 3;
                    MathNumberExpressionSymbol mathNumberExpression = (MathNumberExpressionSymbol) symbolExpressions.get(
                            depthIndex);
                    MathNumberExpressionWrapper expression = new MathNumberExpressionWrapper(mathNumberExpression);
                    expression.setValue(parametersReference.get(i).getOriginalDepthValue());
                }

                MathNumberExpressionSymbol mathNumberExpression = (MathNumberExpressionSymbol) symbolExpressions.get(channelsIndex);
                MathNumberExpressionWrapper expression = new MathNumberExpressionWrapper(mathNumberExpression);
                expression.setValue(parametersReference.get(i).getOriginalChannelValue());

            }
            i += 1;
        }
    }

    private void checkIfScalingFactorsAreBetterThanBest(ScalingFactors scalingFactors) {
        float newAccuracy = trainPipeline.getTrainedAccuracy();
        if (trainPipeline.getTrainedAccuracy() > bestAccuracy) {
            bestAccuracy = newAccuracy;
            bestScalingFactors = scalingFactors;
        }
    }

    private void initVariables() {
        this.bestScalingFactors = new ScalingFactors(1, 1, 1);
        this.bestAccuracy = 0;
    }
}
