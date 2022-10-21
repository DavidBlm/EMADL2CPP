package de.monticore.mlpipelines.parser;

import conflang._symboltable.ConfigurationEntrySymbol;
import de.monticore.mlpipelines.automl.configuration.HyperparameterOptConfig;
import de.monticore.symboltable.Symbol;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public class HyperparameterOptConfigParser extends AbstractConfigurationParser {

    public HyperparameterOptConfig getConfiguration(Path modelPath, String modelName, String scmPath) {
        Map<String, Collection<Symbol>> confSymbols = this.validateConfiguration(modelPath, modelName, scmPath);

        String optimizer = getOptimizer(confSymbols);
        HyperparameterOptConfig hyperparameterOptConfig = new HyperparameterOptConfig(optimizer);
        return hyperparameterOptConfig;
    }

    private String getOptimizer(Map<String, Collection<Symbol>> symbols) {
        ArrayList<Symbol> optimizerList = new ArrayList<>(symbols.get("optimizer"));
        ConfigurationEntrySymbol entrySymbol = (ConfigurationEntrySymbol) optimizerList.get(0);
        return entrySymbol.getValue().toString();
    }
}
