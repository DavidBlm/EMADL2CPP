package de.monticore.mlpipelines.automl.configuration;

public class Configuration {

    private PreprocessingConfig preprocessingConfig;
    private HyperparameterOptConfig hyperparameterOptConfig;
    private EvaluationConfig evaluationConfig;
    private InitialHyperparameters initialHyperparameters;
    private TrainAlgorithmConfig trainAlgorithmConfig;

    public Configuration(){
        this.preprocessingConfig = new PreprocessingConfig();
        this.evaluationConfig = new EvaluationConfig();
        this.initialHyperparameters = new InitialHyperparameters();
        this.trainAlgorithmConfig = new EfficientNetConfig();
    }

    public Configuration(PreprocessingConfig preprocessingConfig,
                         HyperparameterOptConfig hyperparameterOptConfig,
                         EvaluationConfig evaluationConfig,
                         InitialHyperparameters initialHyperparameters,
                         TrainAlgorithmConfig trainAlgorithmConfig) {
        this.preprocessingConfig = preprocessingConfig;
        this.hyperparameterOptConfig = hyperparameterOptConfig;
        this.evaluationConfig = evaluationConfig;
        this.initialHyperparameters = initialHyperparameters;
        this.trainAlgorithmConfig = trainAlgorithmConfig;
    }

    public PreprocessingConfig getPreprocessingConfig() {
        return preprocessingConfig;
    }

    public HyperparameterOptConfig getHyperparameterOptConfig() {
        return hyperparameterOptConfig;
    }

    public EvaluationConfig getEvaluationConfig() {
        return evaluationConfig;
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
