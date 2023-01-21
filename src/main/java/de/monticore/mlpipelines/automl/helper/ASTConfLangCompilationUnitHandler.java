package de.monticore.mlpipelines.automl.helper;

import conflang._ast.ASTConfLangCompilationUnit;
import conflang._ast.ASTConfigurationEntry;
import conflang._ast.ASTNestedConfigurationEntry;
import conflangliterals._ast.ASTTypelessLiteral;
import de.monticore.mcliterals._ast.ASTBooleanLiteral;
import de.monticore.mcliterals._ast.ASTSignedDoubleLiteral;
import de.monticore.mcliterals._ast.ASTSignedIntLiteral;
import de.monticore.mcliterals._ast.ASTStringLiteral;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ASTConfLangCompilationUnitHandler {

    public static Object getValueByKey(ASTConfLangCompilationUnit compilationUnit, String key) {
        List<ASTConfigurationEntry> entries = compilationUnit.getConfiguration().getAllConfigurationEntries();
        ASTConfigurationEntry configurationEntry = entries.stream().filter(entry -> key.equals(entry.getName())).findFirst().orElse(null);
        return getConfigurationEntryValue(configurationEntry);
    }

    public static ASTConfLangCompilationUnit setValueForKey(ASTConfLangCompilationUnit compilationUnit, String key, Object value) {
        List<ASTConfigurationEntry> entries = compilationUnit.getConfiguration().getAllConfigurationEntries();
        ASTConfigurationEntry configurationEntry = entries.stream().filter(entry -> key.equals(entry.getName())).findFirst().orElse(null);
        setConfigurationEntryValue(configurationEntry, value);
        return compilationUnit;
    }

    public static Map<String, Object> getValuesFromNestedConfiguration(ASTConfLangCompilationUnit compilationUnit, String key) {
        Map<String, Object> configMap = new HashMap<>();

        List<ASTConfigurationEntry> entries = compilationUnit.getConfiguration().getAllConfigurationEntries();
        ASTConfigurationEntry configurationEntry = entries.stream().filter(entry -> key.equals(entry.getName())).findFirst().orElse(null);

        List<ASTConfigurationEntry> nestedEntries = ((ASTNestedConfigurationEntry) configurationEntry).getConfigurationEntryList();
        String rootKey = configurationEntry.getName();
        Object rootValue = getConfigurationEntryValue(configurationEntry);
        configMap.put(rootKey, rootValue);

        Map<String, Object> nestedConfigMap = new HashMap<>();
        for (ASTConfigurationEntry entry: nestedEntries) {
            String nestedKey = entry.getName();
            Object nestedValue = getConfigurationEntryValue(entry);
            nestedConfigMap.put(nestedKey, nestedValue);
        }

        configMap.put("nestedMap", nestedConfigMap);
        return configMap;
    }

    public static ASTConfLangCompilationUnit setNestedValueForKeys(ASTConfLangCompilationUnit compilationUnit, String rootKey, String nestedKey, Object value) {
        List<ASTConfigurationEntry> entries = compilationUnit.getConfiguration().getAllConfigurationEntries();
        ASTConfigurationEntry configurationEntry = entries.stream().filter(entry -> rootKey.equals(entry.getName())).findFirst().orElse(null);

        List<ASTConfigurationEntry> nestedEntries = ((ASTNestedConfigurationEntry) configurationEntry).getConfigurationEntryList();
        ASTConfigurationEntry nestedEntry = nestedEntries.stream().filter(entry -> nestedKey.equals(entry.getName())).findFirst().orElse(null);

        setConfigurationEntryValue(nestedEntry, value);
        return compilationUnit;
    }

    private static Object getConfigurationEntryValue(ASTConfigurationEntry configurationEntry) {
        if (configurationEntry.getValue() instanceof ASTTypelessLiteral) {
            return ((ASTTypelessLiteral) configurationEntry.getValue()).getValue();
        } else if (configurationEntry.getValue() instanceof ASTStringLiteral) {
            return  ((ASTStringLiteral) configurationEntry.getValue()).getValue();
        } else if (configurationEntry.getValue() instanceof ASTSignedIntLiteral) {
            return  ((ASTSignedIntLiteral) configurationEntry.getValue()).getValue();
        } else if (configurationEntry.getValue() instanceof ASTSignedDoubleLiteral) {
            return ((ASTSignedDoubleLiteral) configurationEntry.getValue()).getValue();
        } else if (configurationEntry.getValue() instanceof ASTBooleanLiteral) {
            return ((ASTBooleanLiteral) configurationEntry.getValue()).getValue();
        } else {
            throw new IllegalArgumentException("Cannot handle the type of value of ConfigurationEntry.");
        }
    }

    private static void setConfigurationEntryValue(ASTConfigurationEntry configurationEntry, Object newVal) {
        if (configurationEntry.getValue() instanceof ASTTypelessLiteral) {
            ((ASTTypelessLiteral) configurationEntry.getValue()).setValue(newVal.toString());
        } else if (configurationEntry.getValue() instanceof ASTSignedIntLiteral) {
            ((ASTSignedIntLiteral) configurationEntry.getValue()).setSource(newVal.toString());
        } else if (configurationEntry.getValue() instanceof ASTSignedDoubleLiteral) {
            ((ASTSignedDoubleLiteral) configurationEntry.getValue()).setSource(newVal.toString());
        } else if (configurationEntry.getValue() instanceof ASTBooleanLiteral) {
            if ((boolean) newVal) {
                ((ASTBooleanLiteral) configurationEntry.getValue()).setSource(3);
            } else {
                ((ASTBooleanLiteral) configurationEntry.getValue()).setSource(1);
            }
        } else {
            throw new IllegalArgumentException("Cannot handle the type of value of ConfigurationEntry.");
        }
    }

}
