package de.monticore.mlpipelines.parser;

import de.monticore.mlpipelines.automl.configuration.*;

import java.nio.file.Path;
import java.util.Map;

public class ConfFile2ConfigurationParser {
    public ConfFile2ConfigurationParser() {
    }

    public Configuration getConfiguration(Path modelPath, Map<String, String> modelNames) {
        // PreprocessingConfig is ignored for now, since it is not cleared how to manage it
        PreprocessingConfig preprocessingConfig = new PreprocessingConfig(0.8, "normalization", true, false);

        HyperparameterOptConfig hyperparameterOptConfig = getHyperparameterOptConfig(modelPath, modelNames);
        EvaluationConfig evaluationConfig = getEvaluationConfig(modelPath, modelNames);
        InitialHyperparameters initialHyperparameters = getInitialHyperparameters(modelPath, modelNames);
        TrainAlgorithmConfig trainAlgorithmConfig = getTrainAlgorithmConfig(modelPath, modelNames);

        return new Configuration(preprocessingConfig, hyperparameterOptConfig, evaluationConfig, initialHyperparameters, trainAlgorithmConfig);
    }

    private HyperparameterOptConfig getHyperparameterOptConfig(Path modelPath, Map<String, String> modelNames) {
        String hyperparameterOptConf = modelNames.get("HyperparameterOptConf");
        String hyperparameterOptScm = modelNames.get("HyperparameterOptScm");
        HyperparameterOptConfigParser parser = new HyperparameterOptConfigParser();
        return parser.getConfiguration(modelPath, hyperparameterOptConf, hyperparameterOptScm);
    }

    private EvaluationConfig getEvaluationConfig(Path modelPath, Map<String, String> modelNames) {
        String evaluationCriteriaConf = modelNames.get("EvaluationCriteriaConf");
        String evaluationCriteriaScm = modelNames.get("EvaluationCriteriaScm");
        EvaluationConfigParser parser = new EvaluationConfigParser();
        return parser.getConfiguration(modelPath, evaluationCriteriaConf, evaluationCriteriaScm);
    }

    private InitialHyperparameters getInitialHyperparameters(Path modelPath, Map<String, String> modelNames) {
        String networkConf = modelNames.get("NetworkConf");
        String networkScm = modelNames.get("NetworkScm");
        InitialHyperparametersParser parser = new InitialHyperparametersParser();
        return parser.getConfiguration(modelPath, networkConf, networkScm);
    }

    private TrainAlgorithmConfig getTrainAlgorithmConfig(Path modelPath, Map<String, String> modelNames) {
        String trainAlgorithmConf = modelNames.get("TrainAlgorithmConf");
        String trainAlgorithmScm = modelNames.get("TrainAlgorithmScm");
        if (trainAlgorithmConf.equals("EfficientNet")) {
            EfficientNetConfigParser parser = new EfficientNetConfigParser();
            return parser.getConfiguration(modelPath, trainAlgorithmConf, trainAlgorithmScm);
        } else {
            // TODO: Edit if the definition of AdaNetConfig is finished
            return null;
        }
    }
}
