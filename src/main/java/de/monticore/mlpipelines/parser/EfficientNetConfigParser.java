package de.monticore.mlpipelines.parser;

import conflang._symboltable.ConfigurationEntrySymbol;
import de.monticore.mlpipelines.automl.configuration.EfficientNetConfig;
import de.monticore.mlpipelines.automl.trainalgorithms.efficientnet.ScalingFactors;
import de.monticore.symboltable.Symbol;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public class EfficientNetConfigParser extends AbstractConfigurationParser {

    public EfficientNetConfig getConfiguration(Path modelPath, String modelName, String scmPath) {
        Map<String, Collection<Symbol>> confSymbols = this.validateConfiguration(modelPath, modelName, scmPath);

        boolean saveTrainedArchitecture = getSaveTrainedArchitecture(confSymbols);
        String architectureSavePath = getStringValue(confSymbols, "architecture_save_path");
        String trainAlgorithmName = getStringValue(confSymbols, "train_algorithm_name");
        String trainPipelineName = getStringValue(confSymbols, "train_pipeline_name");

        double flopsConditionValue = getFlopsConditionValue(confSymbols);
        ScalingFactors minScalingFactors = getMinScalingFactors(confSymbols);
        ScalingFactors maxScalingFactors = getMaxScalingFactors(confSymbols);
        ScalingFactors scalingFactorsStepSize = getScalingFactorsStepSize(confSymbols);
        int maximumImageWidthAndHeight = getIntValue(confSymbols, "maximum_image_width_and_height");
        int minimumImageWidthAndHeight = getIntValue(confSymbols, "minimum_image_width_and_height");
        int phi = getIntValue(confSymbols, "phi");

        EfficientNetConfig efficientNetConfig = new EfficientNetConfig();
        efficientNetConfig.setSaveTrainedArchitecture(saveTrainedArchitecture);
        efficientNetConfig.setArchitectureSavePath(architectureSavePath);
        efficientNetConfig.setTrainAlgorithmName(trainAlgorithmName);
        efficientNetConfig.setTrainPipelineName(trainPipelineName);

        efficientNetConfig.setFlopsConditionValue(flopsConditionValue);
        efficientNetConfig.setMinScalingFactors(minScalingFactors);
        efficientNetConfig.setMaxScalingFactors(maxScalingFactors);
        efficientNetConfig.setScalingFactorsStepSize(scalingFactorsStepSize);
        efficientNetConfig.setMaximumImageWidthAndHeight(maximumImageWidthAndHeight);
        efficientNetConfig.setMinimumImageWidthAndHeight(minimumImageWidthAndHeight);
        efficientNetConfig.setPhi(phi);

        return efficientNetConfig;
    }

    private boolean getSaveTrainedArchitecture(Map<String, Collection<Symbol>> symbols) {
        ArrayList<Symbol> saveTrainedArchitectureList = new ArrayList<>(symbols.get("save_trained_architecture"));
        ConfigurationEntrySymbol entrySymbol = (ConfigurationEntrySymbol) saveTrainedArchitectureList.get(0);
        return (boolean) entrySymbol.getValue();
    }

    private String getStringValue(Map<String, Collection<Symbol>> symbols, String key) {
        ArrayList<Symbol> symbolList = new ArrayList<>(symbols.get(key));
        ConfigurationEntrySymbol entrySymbol = (ConfigurationEntrySymbol) symbolList.get(0);
        return entrySymbol.getValue().toString();
    }

    private double getFlopsConditionValue(Map<String, Collection<Symbol>> symbols) {
        ArrayList<Symbol> flopsConditionValueList = new ArrayList<>(symbols.get("flops_condition_value"));
        ConfigurationEntrySymbol entrySymbol = (ConfigurationEntrySymbol) flopsConditionValueList.get(0);
        return (double) entrySymbol.getValue();
    }

    private ScalingFactors getMinScalingFactors(Map<String, Collection<Symbol>> symbols) {
        double alpha = getScalingFactorsParam(symbols, "min", "alpha");
        double beta = getScalingFactorsParam(symbols, "min", "beta");
        double gamma = getScalingFactorsParam(symbols, "min", "gamma");
        return new ScalingFactors(alpha, beta, gamma);
    }

    private ScalingFactors getMaxScalingFactors(Map<String, Collection<Symbol>> symbols) {
        double alpha = getScalingFactorsParam(symbols, "max", "alpha");
        double beta = getScalingFactorsParam(symbols, "max", "beta");
        double gamma = getScalingFactorsParam(symbols, "max", "gamma");
        return new ScalingFactors(alpha, beta, gamma);
    }

    private ScalingFactors getScalingFactorsStepSize(Map<String, Collection<Symbol>> symbols) {
        double alpha = getScalingFactorsParam(symbols, "stepsize", "alpha");
        double beta = getScalingFactorsParam(symbols, "stepsize", "beta");
        double gamma = getScalingFactorsParam(symbols, "stepsize", "gamma");
        return new ScalingFactors(alpha, beta, gamma);
    }

    private double getScalingFactorsParam(Map<String, Collection<Symbol>> symbols, String type, String param) {
        String key;
        if (type.equals("stepsize")) {
            key = "scaling_factors_stepsize_" + param;
        } else {
            key = type + "_scaling_factors_" + param;
        }
        ArrayList<Symbol> paramList = new ArrayList<>(symbols.get(key));
        ConfigurationEntrySymbol entrySymbol = (ConfigurationEntrySymbol) paramList.get(0);
        return  (double) entrySymbol.getValue();
    }

    private int getIntValue(Map<String, Collection<Symbol>> symbols, String key) {
        ArrayList<Symbol> symbolList = new ArrayList<>(symbols.get(key));
        ConfigurationEntrySymbol entrySymbol = (ConfigurationEntrySymbol) symbolList.get(0);
        return (int) entrySymbol.getValue();
    }
}
