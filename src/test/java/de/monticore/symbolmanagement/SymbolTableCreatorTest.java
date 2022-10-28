package de.monticore.symbolmanagement;

import de.monticore.io.paths.ModelPath;
import de.monticore.lang.embeddedmontiarc.embeddedmontiarc._ast.ASTEMACompilationUnit;
import de.monticore.lang.embeddedmontiarc.embeddedmontiarc._symboltable.instanceStructure.EMAComponentInstanceSymbol;
import de.monticore.lang.monticar.emadl._symboltable.EMADLLanguage;
import de.monticore.parsing.EMADLParser;
import de.monticore.symboltable.GlobalScope;
import de.monticore.symboltable.Scope;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class SymbolTableCreatorTest {

    //TODO more validations on symbol table ?
    @Test
    void createEMADLSymbolTable() throws IOException {
        final ModelPath modelPath = new ModelPath(Paths.get("src/test/resources/models/pipeline"));
        final ASTEMACompilationUnit parsedModel = (ASTEMACompilationUnit) new EMADLParser().parseModelOrThrowException("src/test/resources/models/pipeline/LinearPipeline.ema");
        final Scope symbolTable = SymbolTableCreator.createEMADLSymbolTable(parsedModel, new GlobalScope(modelPath, new EMADLLanguage()));
        final Optional<EMAComponentInstanceSymbol> emaInstanceComponent = symbolTable.resolve("linearPipeline", EMAComponentInstanceSymbol.KIND);
        assertAll(() -> assertEquals(2, symbolTable.getSubScopes().size()),
                () -> assertTrue(emaInstanceComponent.isPresent())
        );
    }
}