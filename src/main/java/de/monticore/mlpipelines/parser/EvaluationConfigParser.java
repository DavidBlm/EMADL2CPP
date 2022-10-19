package de.monticore.mlpipelines.parser;

import conflang._symboltable.ConfigurationEntrySymbol;
import de.monticore.mlpipelines.automl.configuration.EvaluationConfig;
import de.monticore.symboltable.Symbol;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public class EvaluationConfigParser extends AbstractConfigurationParser {

    public EvaluationConfig getConfiguration(Path modelPath, String modelName, String scmPath) {
        Map<String, Collection<Symbol>> confSymbols = this.validateConfiguration(modelPath, modelName, scmPath);

        String metric = getMetric(confSymbols);
        double acceptanceRate = getAcceptanceRate(confSymbols);
        return new EvaluationConfig(metric, acceptanceRate);
    }

    private String getMetric(Map<String, Collection<Symbol>> symbols) {
        ArrayList<Symbol> metricList = new ArrayList<>(symbols.get("metric"));
        ConfigurationEntrySymbol entrySymbol = (ConfigurationEntrySymbol) metricList.get(0);
        return entrySymbol.getValue().toString();
    }

    private double getAcceptanceRate(Map<String, Collection<Symbol>> symbols) {
        ArrayList<Symbol> acceptanceRateList = new ArrayList<>(symbols.get("acceptance_rate"));
        ConfigurationEntrySymbol entrySymbol = (ConfigurationEntrySymbol) acceptanceRateList.get(0);
        return (double) entrySymbol.getValue();
    }
}
