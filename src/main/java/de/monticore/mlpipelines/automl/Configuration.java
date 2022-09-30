package de.monticore.mlpipelines.automl;

import java.util.Hashtable;

public class Configuration {

    private Hashtable<String, Object> preprocessingConfig;
    private String hyperparameterOptimizerConfig;
    private Hashtable<String, Object> evaluationConfig;
    private Hashtable<String, Object> networkConfig;
    private Hashtable<String, Object> initialHyperparameters;
    private TrainAlgorithmConfig trainAlgorithmConfig;

    public Configuration(Hashtable<String, Object> preprocessingConfig,
                         String hyperparameterOptimizerConfig,
                         Hashtable<String, Object> evaluationConfig,
                         Hashtable<String, Object> networkConfig,
                         Hashtable<String, Object> initialHyperparameters,
                         TrainAlgorithmConfig trainAlgorithmConfig) {
        this.preprocessingConfig = preprocessingConfig;
        this.hyperparameterOptimizerConfig = hyperparameterOptimizerConfig;
        this.evaluationConfig = evaluationConfig;
        this.networkConfig = networkConfig;
        this.initialHyperparameters = initialHyperparameters;
        this.trainAlgorithmConfig = trainAlgorithmConfig;
    }

    public Hashtable<String, Object> getPreprocessingConfig() {
        return preprocessingConfig;
    }

    public String getHyperparameterOptimizerConfig() {
        return hyperparameterOptimizerConfig;
    }

    public Hashtable<String, Object> getEvaluationConfig() {
        return evaluationConfig;
    }

    public Hashtable<String, Object> getNetworkConfig() {
        return networkConfig;
    }

    public Hashtable<String, Object> getInitialHyperparameters() {
        return initialHyperparameters;
    }

    public TrainAlgorithmConfig getTrainAlgorithmConfig() {
        return trainAlgorithmConfig;
    }
}
