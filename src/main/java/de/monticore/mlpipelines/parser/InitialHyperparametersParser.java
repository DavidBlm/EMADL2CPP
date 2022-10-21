package de.monticore.mlpipelines.parser;

import conflang._symboltable.ConfigurationEntrySymbol;
import conflang._symboltable.NestedConfigurationEntrySymbol;
import de.monticore.mlpipelines.automl.configuration.InitialHyperparameters;
import de.monticore.symboltable.Symbol;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Map;

public class InitialHyperparametersParser extends AbstractConfigurationParser {

    public InitialHyperparameters getConfiguration(Path modelPath, String modelName, String scmPath) {
        Map<String, Collection<Symbol>> confSymbols = this.validateConfiguration(modelPath, modelName, scmPath);

        int numEpochs = getNumEpochs(confSymbols);
        int batchSize = getBatchSize(confSymbols);
        boolean normalize = getNormalize(confSymbols);
        String context = getContext(confSymbols);
        boolean loadCheckpoint = getLoadCheckpoint(confSymbols);
        Hashtable<String, Object> optimizer = getOptimizer(confSymbols);

        return new InitialHyperparameters(numEpochs, batchSize, normalize, context, loadCheckpoint, optimizer);
    }

    private int getNumEpochs(Map<String, Collection<Symbol>> symbols) {
        ArrayList<Symbol> numEpochsList = new ArrayList<>(symbols.get("num_epoch"));
        ConfigurationEntrySymbol entrySymbol = (ConfigurationEntrySymbol) numEpochsList.get(0);
        return (int) entrySymbol.getValue();
    }

    private int getBatchSize(Map<String, Collection<Symbol>> symbols) {
        ArrayList<Symbol> batchSizeList = new ArrayList<>(symbols.get("batch_size"));
        ConfigurationEntrySymbol entrySymbol = (ConfigurationEntrySymbol) batchSizeList.get(0);
        return (int) entrySymbol.getValue();
    }

    private boolean getNormalize(Map<String, Collection<Symbol>> symbols) {
        ArrayList<Symbol> normalizeList = new ArrayList<>(symbols.get("normalize"));
        ConfigurationEntrySymbol entrySymbol = (ConfigurationEntrySymbol) normalizeList.get(0);
        return (boolean) entrySymbol.getValue();
    }

    private String getContext(Map<String, Collection<Symbol>> symbols) {
        ArrayList<Symbol> acceptanceRateList = new ArrayList<>(symbols.get("context"));
        ConfigurationEntrySymbol entrySymbol = (ConfigurationEntrySymbol) acceptanceRateList.get(0);
        return entrySymbol.getValue().toString();
    }

    private boolean getLoadCheckpoint(Map<String, Collection<Symbol>> symbols) {
        ArrayList<Symbol> loadCheckpointList = new ArrayList<>(symbols.get("load_checkpoint"));
        ConfigurationEntrySymbol entrySymbol = (ConfigurationEntrySymbol) loadCheckpointList.get(0);
        return (boolean) entrySymbol.getValue();
    }

    private Hashtable<String, Object> getOptimizer(Map<String, Collection<Symbol>> symbols) {
        Hashtable<String, Object> optimizerTable = new Hashtable<>();
        NestedConfigurationEntrySymbol optimizerSymbol = (NestedConfigurationEntrySymbol) new ArrayList<>(symbols.get("optimizer")).get(0);
        optimizerTable.put(optimizerSymbol.getName(), optimizerSymbol.getValue());

        Map<String, Collection<Symbol>> optimizerSymbols = optimizerSymbol.getSpannedScope().getLocalSymbols();
        for (Map.Entry<String, Collection<Symbol>> optSymbol : optimizerSymbols.entrySet()) {
            ConfigurationEntrySymbol symbolPair;
            symbolPair = (ConfigurationEntrySymbol) ((ArrayList<?>) optSymbol.getValue()).get(0);
            optimizerTable.put(symbolPair.getName(), symbolPair.getValue());
        }

        return optimizerTable;
    }
}
