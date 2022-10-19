package de.monticore.mlpipelines.parser;

import conflang._cocos.ConfLangCoCoChecker;
import conflang._cocos.ConfLangCocoFactory;
import conflang._symboltable.ConfLangLanguage;
import conflang._symboltable.ConfigurationSymbol;
import de.monticore.ModelingLanguageFamily;
import de.monticore.io.paths.ModelPath;
import de.monticore.lang.embeddedmontiarc.embeddedmontiarc._symboltable.EmbeddedMontiArcLanguage;
import de.monticore.lang.monticar.cnnarch.generator.GenerationAbortedException;
import de.monticore.symboltable.GlobalScope;
import de.monticore.symboltable.Symbol;
import de.se_rwth.commons.logging.Log;
import schemalang._cocos.SchemaLangCoCoChecker;
import schemalang._cocos.SchemaLangCocoFactory;
import schemalang._symboltable.SchemaDefinitionSymbol;
import schemalang._symboltable.SchemaLangLanguage;
import schemalang.validation.ReferenceModelViolation;
import schemalang.validation.SchemaLangValidator;
import schemalang.validation.SchemaViolation;
import schemalang.validation.Violation;
import schemalang.validation.exception.SchemaLangException;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.*;

public abstract class AbstractConfigurationParser {

    private ConfigurationSymbol resolveConfiguration(Path modelPath, String modelName) {
        ModelPath mp = new ModelPath(new Path[]{modelPath});
        GlobalScope scope = new GlobalScope(mp, new ConfLangLanguage());
        Optional<ConfigurationSymbol> configurationSymbolOpt = scope.resolve(modelName, ConfigurationSymbol.KIND);
        if (!configurationSymbolOpt.isPresent()) {
            String message = String.format("Could not resolve configuration for model '%s'.", modelName);
            Log.error(message);
            throw new RuntimeException(message);
        } else {
            ConfigurationSymbol configurationSymbol = configurationSymbolOpt.get();
            ConfLangCoCoChecker checkerWithAllCoCos = ConfLangCocoFactory.createCheckerWithAllCoCos();
            checkerWithAllCoCos.checkAll(configurationSymbol.getConfigurationNode().get());
            return configurationSymbol;
        }
    }

    private ModelPath getSchemaModelPath() {
        URL schemasResource = this.getClass().getClassLoader().getResource("schemas/");
        try {
            Path path = getSchemaPath(schemasResource);

            ModelPath modelPath = new ModelPath(new Path[]{path});
            return modelPath;
        } catch (RuntimeException e) {
            Log.error(e.getMessage(), e);
            throw new GenerationAbortedException("Generation aborted due to errors in the configuration.");
        }
    }

    private Path getSchemaPath(URL schemasResource) {
        try {
            FileSystem fileSystem = this.initFileSystem(schemasResource.toURI());
            String pathString = schemasResource.getPath();
            if (isWindows())
                pathString = pathString.substring(1);
            Path path = fileSystem.getPath(pathString);
            return path;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }

    private SchemaDefinitionSymbol resolveSchemaDefinition(String modelName) {
        ModelPath modelPath = this.getSchemaModelPath();
        ModelingLanguageFamily family = new ModelingLanguageFamily();
        family.addModelingLanguage(new SchemaLangLanguage());
        family.addModelingLanguage(new EmbeddedMontiArcLanguage());
        GlobalScope globalScope = new GlobalScope(modelPath, family);
        Optional<SchemaDefinitionSymbol> compilationUnit = globalScope.resolve(modelName, SchemaDefinitionSymbol.KIND);
        if (!compilationUnit.isPresent()) {
            String message = String.format("Could not resolve schema definition for model '%s' in model path '%s'.",
                    modelName, modelPath);
            Log.error(message);
            throw new RuntimeException(message);
        } else {
            SchemaDefinitionSymbol schemaDefinitionSymbol = compilationUnit.get();
            SchemaLangCoCoChecker checkerWithAllCoCos = SchemaLangCocoFactory.getCheckerWithAllCoCos();
            checkerWithAllCoCos.checkAll(schemaDefinitionSymbol.getSchemaDefinitionNode().get());
            return schemaDefinitionSymbol;
        }
    }

    private FileSystem initFileSystem(URI uri) throws IOException {
        try {
            return FileSystems.getDefault();
        } catch (Exception e) {
            Map<String, String> env = new HashMap();
            env.put("create", "true");
            return FileSystems.newFileSystem(uri, env);
        }
    }

    protected Map<String, Collection<Symbol>> validateConfiguration(Path modelPath, String modelName, String scmName) {
        ConfigurationSymbol configurationSymbol = resolveConfiguration(modelPath, modelName);
        SchemaDefinitionSymbol schemaDefinitionSymbol = resolveSchemaDefinition(scmName);

        try {
            List<SchemaViolation> violations = SchemaLangValidator.validateConfiguration(schemaDefinitionSymbol, configurationSymbol);
            if (!violations.isEmpty()) {
                this.logViolations(violations);
                throw new GenerationAbortedException("Generation aborted due to errors in the configuration.");
            }
            return configurationSymbol.getSpannedScope().getLocalSymbols();
        } catch (SchemaLangException e) {
            throw new RuntimeException(e);
        }
    }

    private void logViolations(Collection<? extends Violation> violations) {
        Iterator violationsIterator = violations.iterator();

        while(violationsIterator.hasNext()) {
            Violation violation = (Violation)violationsIterator.next();
            if (violation instanceof ReferenceModelViolation) {
                ReferenceModelViolation referenceModelViolation = (ReferenceModelViolation)violation;
                Log.error(referenceModelViolation.toString());
                Collection<Violation> rmViolations = referenceModelViolation.getViolations();
                this.logViolations(rmViolations);
            } else {
                Log.error(violation.toString());
            }
        }

    }
}
