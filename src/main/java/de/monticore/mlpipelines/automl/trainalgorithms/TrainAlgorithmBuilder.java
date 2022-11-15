package de.monticore.mlpipelines.automl.trainalgorithms;

import de.monticore.mlpipelines.Pipeline;
import de.monticore.mlpipelines.automl.configuration.TrainAlgorithmConfig;
import de.monticore.mlpipelines.automl.trainalgorithms.adanet.AdaNetAlgorithm;
import de.monticore.mlpipelines.automl.trainalgorithms.efficientnet.EfficientNet;
import de.monticore.mlpipelines.python.PythonPipeline;


public class TrainAlgorithmBuilder {
    private final String EfficientNet = "EfficientNet";
    private final String AdaNet = "AdaNet";
    private final String PytorchPipeline = "Pytorch";

    private TrainAlgorithmConfig config;

    public void setConfig(TrainAlgorithmConfig config) {
        this.config = config;
    }

    public NeuralArchitectureSearch build() {
        NeuralArchitectureSearch neuralArchitectureSearch = getTrainAlgorithm();
        Pipeline trainPipeline = getPipeline();
        neuralArchitectureSearch.setTrainPipeline(trainPipeline);
        neuralArchitectureSearch.setTrainConfiguration(config);
        return neuralArchitectureSearch;
    }

    private NeuralArchitectureSearch getTrainAlgorithm() {
        switch (config.getTrainAlgorithmName()) {
            case EfficientNet:
                return new EfficientNet();
            case AdaNet:
                String modelPath = "src.test.resources.models.adanet.AdaNet.emadl";
                return new AdaNetAlgorithm();
            default:
                throw new IllegalArgumentException(
                        "Train algorithm " + config.getTrainAlgorithmName() + " not supported");
        }
    }

    private Pipeline getPipeline() {
        if (PytorchPipeline.equals(config.getTrainPipelineName())) {
            return new PythonPipeline();
        }
        throw new IllegalArgumentException("Pipeline " + config.getTrainPipelineName() + " not supported");
    }
}
