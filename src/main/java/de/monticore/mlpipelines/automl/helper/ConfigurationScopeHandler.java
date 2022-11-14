package de.monticore.mlpipelines.automl.helper;

import conflang._symboltable.ConfigurationEntry;
import conflang._symboltable.ConfigurationEntrySymbol;
import conflang._symboltable.ConfigurationScope;
import conflang._symboltable.NestedConfigurationEntrySymbol;
import de.monticore.symboltable.Symbol;

import java.util.*;

public class ConfigurationScopeHandler {

    public static Object getValueByKey(ConfigurationScope scope, String key) {
        Map<String, Collection<Symbol>> symbols = scope.getLocalSymbols();
        ArrayList<Symbol> keyList = new ArrayList<>(symbols.get(key));
        return ((ConfigurationEntrySymbol) keyList.get(0)).getValue();
    }

    public static ConfigurationScope setValueForKey(ConfigurationScope scope, String key, Object value) {
        Map<String, Collection<Symbol>> symbols = scope.getLocalSymbols();
        ArrayList<Symbol> keyList = new ArrayList<>(symbols.get(key));
        ((ConfigurationEntrySymbol) keyList.get(0)).setValue(value);
        return scope;
    }

    public static Map<String, Object> getValuesFromNestedConfiguration(ConfigurationScope scope, String key) {
        Map<String, Object> configMap = new HashMap<>();

        Map<String, Collection<Symbol>> symbols = scope.getLocalSymbols();
        ArrayList<Symbol> keyList = new ArrayList<>(symbols.get(key));
        String rootKey = keyList.get(0).getName();
        Object rootValue = ((NestedConfigurationEntrySymbol) keyList.get(0)).getValue();
        configMap.put(rootKey, rootValue);

        List<ConfigurationEntry> nestedParamsList = ((NestedConfigurationEntrySymbol) keyList.get(0)).getAllConfigurationEntries();
        Map<String, Object> nestedConfigMap = new HashMap<>();
        for (ConfigurationEntry entry: nestedParamsList) {
            String nestedKey = entry.getName();
            Object nestedValue = entry.getValue();
            nestedConfigMap.put(nestedKey, nestedValue);
        }

        configMap.put("nestedMap", nestedConfigMap);

        return configMap;
    }

    public static ConfigurationScope setNestedValueForKeys(ConfigurationScope scope, String rootKey, String nestedKey, Object value) {
        Map<String, Collection<Symbol>> symbols = scope.getLocalSymbols();
        ArrayList<Symbol> keyList = new ArrayList<>(symbols.get(rootKey));
        List<ConfigurationEntry> nestedParamsList = ((NestedConfigurationEntrySymbol) keyList.get(0)).getAllConfigurationEntries();
        ConfigurationEntry configurationEntry = nestedParamsList.stream().filter(entry -> nestedKey.equals(entry.getName())).findFirst().orElse(null);
        configurationEntry.setValue(value);
        return scope;
    }

}
