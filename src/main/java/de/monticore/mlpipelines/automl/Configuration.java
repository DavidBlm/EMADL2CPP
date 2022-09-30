package de.monticore.mlpipelines.automl;

import java.util.Hashtable;

public class Configuration {

    private Hashtable<String, String> preprocessingConfig;
    private Hashtable<String, String> hyperparameterOptimizerConfig;
    private Hashtable<String, String> evaluationConfig;
    private Hashtable<String, String> networkConfig;
    private Hashtable<String, String> initialHyperparameters;
    private TrainAlgorithmConfig trainAlgorithmConfig;

    public Configuration(Hashtable<String, String> preprocessingConfig,
                         Hashtable<String, String> hyperparameterOptimizerConfig,
                         Hashtable<String, String> evaluationConfig,
                         Hashtable<String, String> networkConfig,
                         Hashtable<String, String> initialHyperparameters,
                         TrainAlgorithmConfig trainAlgorithmConfig) {
        this.preprocessingConfig = preprocessingConfig;
        this.hyperparameterOptimizerConfig = hyperparameterOptimizerConfig;
        this.evaluationConfig = evaluationConfig;
        this.networkConfig = networkConfig;
        this.initialHyperparameters = initialHyperparameters;
        this.trainAlgorithmConfig = trainAlgorithmConfig;
    }

    public Hashtable<String, String> getPreprocessingConfig() {
        return preprocessingConfig;
    }

    public Hashtable<String, String> getHyperparameterOptimizerConfig() {
        return hyperparameterOptimizerConfig;
    }

    public Hashtable<String, String> getEvaluationConfig() {
        return evaluationConfig;
    }

    public Hashtable<String, String> getNetworkConfig() {
        return networkConfig;
    }

    public Hashtable<String, String> getInitialHyperparameters() {
        return initialHyperparameters;
    }

    public TrainAlgorithmConfig getTrainAlgorithmConfig() {
        return trainAlgorithmConfig;
    }
}
