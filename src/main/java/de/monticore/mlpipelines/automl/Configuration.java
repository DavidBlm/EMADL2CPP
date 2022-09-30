package de.monticore.mlpipelines.automl;

import java.util.Hashtable;

public class Configuration {

    Hashtable<String, String> preprocessingConfig;
    Hashtable<String, String> hyperparameterOptimizerConfig;
    Hashtable<String, String> evaluationConfig;
    Hashtable<String, String> networkConfig;
    Hashtable<String, String> initialHyperparameters;
    TrainAlgorithmConfig trainAlgorithmConfig;

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
}
