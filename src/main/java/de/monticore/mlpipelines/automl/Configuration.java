package de.monticore.mlpipelines.automl;

import java.util.Hashtable;

public class Configuration {

    Hashtable<String, String> preprocessingConfig = new Hashtable<>();
    Hashtable<String, String> hyperparameterOptimizerConfig = new Hashtable<>();
    Hashtable<String, String> evaluationConfig = new Hashtable<>();
    Hashtable<String, String> networkConfig = new Hashtable<>();
    Hashtable<String, String> initialHyperparameters = new Hashtable<>();
    TrainAlgorithmConfig trainAlgorithmConfig = new TrainAlgorithmConfig();

}
