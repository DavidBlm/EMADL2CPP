package de.monticore.mlpipelines.automl.configuration;

import java.util.Hashtable;

public class Configuration {

    private PreprocessingConfig preprocessingConfig;
    private String hyperparameterOptimizerConfig;
    private EvaluationConfig evaluationConfig;
    private NetworkConfig networkConfig;
    private final Hashtable<String, Object> initialHyperparameters;
    private TrainAlgorithmConfig trainAlgorithmConfig;

    public Configuration(){
        this.preprocessingConfig = new PreprocessingConfig();
        this.evaluationConfig = new EvaluationConfig();
        this.networkConfig = new NetworkConfig();
        this.initialHyperparameters = new Hashtable<>();
        this.trainAlgorithmConfig = new TrainAlgorithmConfig();
    }

    public Configuration(PreprocessingConfig preprocessingConfig,
                         String hyperparameterOptimizerConfig,
                         EvaluationConfig evaluationConfig,
                         NetworkConfig networkConfig,
                         Hashtable<String, Object> initialHyperparameters,
                         TrainAlgorithmConfig trainAlgorithmConfig) {
        this.preprocessingConfig = preprocessingConfig;
        this.hyperparameterOptimizerConfig = hyperparameterOptimizerConfig;
        this.evaluationConfig = evaluationConfig;
        this.networkConfig = networkConfig;
        this.initialHyperparameters = initialHyperparameters;
        this.trainAlgorithmConfig = trainAlgorithmConfig;
    }

    public PreprocessingConfig getPreprocessingConfig() {
        return preprocessingConfig;
    }

    public String getHyperparameterOptimizerConfig() {
        return hyperparameterOptimizerConfig;
    }

    public EvaluationConfig getEvaluationConfig() {
        return evaluationConfig;
    }

    public NetworkConfig getNetworkConfig() {
        return networkConfig;
    }

    public Hashtable<String, Object> getInitialHyperparameters() {
        return initialHyperparameters;
    }

    public TrainAlgorithmConfig getTrainAlgorithmConfig() {
        return trainAlgorithmConfig;
    }
    public void setTrainAlgorithmConfig(TrainAlgorithmConfig trainAlgorithmConfig) {
        this.trainAlgorithmConfig = trainAlgorithmConfig;
    }
}
