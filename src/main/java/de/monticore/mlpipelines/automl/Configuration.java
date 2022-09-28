package de.monticore.mlpipelines.pipeline.automl;

import de.monticore.mlpipelines.pipeline.Pipeline;

public class Configuration {
    Hashtable<String, String> preprocessing_config = new Hashtable<String, String>();
    Hashtable<String, String> hyperparameter_optimizer_config = new Hashtable<String, String>();
    Hashtable<String, String> evaluation_config = new Hashtable<String, String>();
    Hashtable<String, String> network_config = new Hashtable<String, String>();
    Hashtable<String, String> initial_hyperparameters = new Hashtable<String, String>();

    TrainAlgorithmConfig train_algorithm_config = new TrainAlgorithmConfig()
}