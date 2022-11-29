package de.monticore.mlpipelines.automl;

import de.monticore.lang.monticar.cnnarch._symboltable.ArchitectureSymbol;
import de.monticore.mlpipelines.Pipeline;
import de.monticore.mlpipelines.automl.configuration.Configuration;
import de.monticore.mlpipelines.automl.configuration.HyperparameterOptConfig;
import de.monticore.mlpipelines.automl.configuration.TrainAlgorithmConfig;
import de.monticore.mlpipelines.automl.hyperparameters.HyperparameterAlgorithm;
import de.monticore.mlpipelines.automl.trainalgorithms.NeuralArchitectureSearch;
import de.monticore.mlpipelines.automl.trainalgorithms.TrainAlgorithmBuilder;

public class AutoMLPipeline extends Pipeline {
    private Configuration configuration;
    private Pipeline trainPipeline;
    private ArchitectureSymbol architecture;
    private NeuralArchitectureSearch neuralArchitectureSearch;
    private HyperparameterAlgorithm hyperparameterAlgorithm;
    private TrainAlgorithmBuilder trainAlgorithmBuilder;

    public AutoMLPipeline() {
        trainAlgorithmBuilder = new TrainAlgorithmBuilder();
    }

    @Override
    public void execute(ArchitectureSymbol originalArchitecture, Configuration configuration) {
        executeNeuralArchitectureSearch(originalArchitecture);
        executeHyperparameterOptimization(configuration);
        trainPipeline.execute(architecture, configuration);
    }

    private void executeNeuralArchitectureSearch(ArchitectureSymbol originalArchitecture) {
        loadTrainAlgorithm();
        architecture = neuralArchitectureSearch.execute(originalArchitecture);
    }

    private void executeHyperparameterOptimization(Configuration configuration) {
        loadHyperparameterAlgorithm();
        this.configuration = configuration;
        hyperparameterAlgorithm.executeIteration();
    }

    public void loadTrainAlgorithm() {
        TrainAlgorithmConfig trainAlgorithmConfig = configuration.getTrainAlgorithmConfig();
        trainAlgorithmBuilder.setConfig(trainAlgorithmConfig);
        this.neuralArchitectureSearch = trainAlgorithmBuilder.build();
    }

    private void loadHyperparameterAlgorithm() {
        HyperparameterOptConfig hyperparameterOptConfig = configuration.getHyperparameterOptConfig();
        //TODO: Use HyperparameterAlgorithmBuilder to load the correct algorithm
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public Pipeline getTrainPipeline() {
        return trainPipeline;
    }

    public NeuralArchitectureSearch getTrainAlgorithm() {
        return neuralArchitectureSearch;
    }

    public ArchitectureSymbol getArchitecture() {
        return architecture;
    }

    public TrainAlgorithmBuilder getTrainAlgorithmBuilder() {
        return trainAlgorithmBuilder;
    }

    public void setTrainAlgorithmBuilder(TrainAlgorithmBuilder trainAlgorithmBuilder) {
        this.trainAlgorithmBuilder = trainAlgorithmBuilder;
    }
}
