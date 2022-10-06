package de.monticore.mlpipelines.automl.configuration;

public class Configuration {

    private PreprocessingConfig preprocessingConfig;
    private String hyperparameterOptimizerConfig;
    private EvaluationConfig evaluationConfig;
    private NetworkConfig networkConfig;
    private InitialHyperparameters initialHyperparameters;
    private TrainAlgorithmConfig trainAlgorithmConfig;

    public Configuration(){
        this.preprocessingConfig = new PreprocessingConfig();
        this.evaluationConfig = new EvaluationConfig();
        this.networkConfig = new NetworkConfig();
        this.initialHyperparameters = new InitialHyperparameters();
        this.trainAlgorithmConfig = new EfficientNetConfig();
    }

    public Configuration(PreprocessingConfig preprocessingConfig,
                         String hyperparameterOptimizerConfig,
                         EvaluationConfig evaluationConfig,
                         NetworkConfig networkConfig,
                         InitialHyperparameters initialHyperparameters,
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

    public InitialHyperparameters getInitialHyperparameters() {
        return initialHyperparameters;
    }

    public TrainAlgorithmConfig getTrainAlgorithmConfig() {
        return trainAlgorithmConfig;
    }
    public void setTrainAlgorithmConfig(TrainAlgorithmConfig trainAlgorithmConfig) {
        this.trainAlgorithmConfig = trainAlgorithmConfig;
    }
}
