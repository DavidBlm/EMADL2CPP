package de.monticore.mlpipelines.parser;

import conflang.ConfLangFacade;
import conflang._symboltable.ConfigurationEntry;
import conflang._symboltable.ConfigurationEntrySymbol;
import conflang._symboltable.ConfigurationScope;
import conflang._symboltable.NestedConfigurationEntrySymbol;
import de.monticore.mlpipelines.automl.configuration.Configuration;
import de.monticore.mlpipelines.automl.configuration.EvaluationConfig;
import de.monticore.mlpipelines.automl.configuration.PreprocessingConfig;
import de.monticore.mlpipelines.automl.configuration.TrainAlgorithmConfig;
import de.monticore.symboltable.Symbol;

import java.nio.file.Path;
import java.security.KeyException;
import java.util.*;

public class ConfFile2ConfigurationParser {

    private final Configuration configuration;

    public ConfFile2ConfigurationParser(Path modelPath, String modelName) {
        ConfigurationScope artifactScope = ConfLangFacade.create(modelPath, modelName).getArtifactScope();
        Map<String, Collection<Symbol>> confSymbols = artifactScope.getLocalSymbols();

        List<ConfigurationEntry> preprocessingEntries = getConfEntriesByKey(confSymbols, "preprocessing");
        List<ConfigurationEntry> evaluationEntries = getConfEntriesByKey(confSymbols, "evaluation");
        List<ConfigurationEntry> networkEntries = getConfEntriesByKey(confSymbols, "network");
        List<ConfigurationEntry> initialHyperparametersEntries = getConfEntriesByKey(confSymbols, "initial_hyperparameters");
        List<ConfigurationEntry> trainAlgorithmEntries = getConfEntriesByKey(confSymbols, "train_algorithm");

        String hyperparameterOptimizerConfig = getHyperparameterOptimizer(confSymbols);
        Hashtable<String, Object> networkConfig = getConfigByEntries(networkEntries);
        Hashtable<String, Object> initialHyperparameters = getConfigByEntries(initialHyperparametersEntries);
        try {
            TrainAlgorithmConfig trainAlgorithmConfig = getTrainAlgorithmConfigByEntries(trainAlgorithmEntries);
            EvaluationConfig evaluationConfig = getEvaluationConfigByEntries(evaluationEntries);
            PreprocessingConfig preprocessingConfig = getPreprocessingConfigByEntries(preprocessingEntries);
            this.configuration = new Configuration(preprocessingConfig, hyperparameterOptimizerConfig, evaluationConfig, networkConfig, initialHyperparameters, trainAlgorithmConfig);
        } catch (KeyException e) {
            throw new RuntimeException(e);
        }
    }

    public Configuration getConfiguration() {
        return this.configuration;
    }

    private String getHyperparameterOptimizer(Map<String, Collection<Symbol>> symbols) {
        ArrayList<Symbol> hyperparameterOptimizerList = new ArrayList<>(symbols.get("hyperparameter_optimizer"));
        ConfigurationEntrySymbol entrySymbol = (ConfigurationEntrySymbol) hyperparameterOptimizerList.get(0);
        return entrySymbol.getValue().toString();
    }

    private List<ConfigurationEntry> getConfEntriesByKey(Map<String, Collection<Symbol>> symbols, String key) {
        ArrayList<Symbol> keyList = new ArrayList<>(symbols.get(key));
        return ((NestedConfigurationEntrySymbol) keyList.get(0)).getAllConfigurationEntries();
    }

    private Hashtable<String, Object> getConfigByEntries(List<ConfigurationEntry> entries) {
        Hashtable<String, Object> configTable = new Hashtable<>();
        for (ConfigurationEntry entry : entries) {
            if (entry.getName().equals("optimizer")) {
                Hashtable<String, Object> optimizerConfig = new Hashtable<>();
                optimizerConfig.put(entry.getName(), entry.getValue());
                Map<String, Collection<Symbol>> optimizerSymbols = ((NestedConfigurationEntrySymbol) entry).getSpannedScope().getLocalSymbols();
                for (Map.Entry<String, Collection<Symbol>> optSymbol : optimizerSymbols.entrySet()) {
                    ConfigurationEntrySymbol symbolPair;
                    symbolPair = (ConfigurationEntrySymbol) ((ArrayList<?>) optSymbol.getValue()).get(0);
                    optimizerConfig.put(symbolPair.getName(), symbolPair.getValue());
                }
                configTable.put("optimizer", optimizerConfig);
            } else {
                configTable.put(entry.getName(), entry.getValue());
            }
        }
        return configTable;
    }

    private EvaluationConfig getEvaluationConfigByEntries(List<ConfigurationEntry> entries) throws KeyException {
        String metric = (String) getConfigurationEntryValue(entries, "metric");
        double acceptanceRate = (double) getConfigurationEntryValue(entries, "acceptance_rate");

        return new EvaluationConfig(metric, acceptanceRate);
    }

    private PreprocessingConfig getPreprocessingConfigByEntries(List<ConfigurationEntry> entries) throws KeyException {
        double trainSplit = (double) getConfigurationEntryValue(entries, "train_split");
        String normMethod = (String) getConfigurationEntryValue(entries, "norm_method");
        boolean grayscale = (boolean) getConfigurationEntryValue(entries, "grayscale");
        boolean dataAugmentation = (boolean) getConfigurationEntryValue(entries, "data_augmentation");

        return new PreprocessingConfig(trainSplit, normMethod, grayscale, dataAugmentation);
    }

    private TrainAlgorithmConfig getTrainAlgorithmConfigByEntries(List<ConfigurationEntry> entries) throws KeyException {
        TrainAlgorithmConfig config = new TrainAlgorithmConfig();

        int numEpochs = (int) getConfigurationEntryValue(entries, "num_epochs");
        boolean saveTrainedArchitecture = (boolean) getConfigurationEntryValue(entries, "save_trained_architecture");
        String architectureSavePath = (String) getConfigurationEntryValue(entries, "architecture_save_path");
        String trainAlgorithmName = (String) getConfigurationEntryValue(entries, "train_algorithm_name");
        String trainPipelineName = (String) getConfigurationEntryValue(entries, "train_pipeline_name");

        config.setNumEpochs(numEpochs);
        config.setSaveTrainedArchitecture(saveTrainedArchitecture);
        config.setArchitectureSavePath(architectureSavePath);
        config.setTrainAlgorithmName(trainAlgorithmName);
        config.setTrainPipelineName(trainPipelineName);

        return config;
    }

    private Object getConfigurationEntryValue(List<ConfigurationEntry> entries, String key) throws KeyException {
        for (ConfigurationEntry entry : entries) {
            if (entry.getName().equals(key)) {
                return entry.getValue();
            }
        }
        throw new KeyException(String.format("Key %s not in entries", key));
    }
}
