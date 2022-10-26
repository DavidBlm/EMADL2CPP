package de.monticore.parsing;

import de.monticore.lang.embeddedmontiarc.embeddedmontiarc._ast.ASTEMACompilationUnit;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EmbeddedMontiArcLanguageParserTest {

    @Test
    void exceptionByParsing() {
        assertThrows(IOException.class, () -> new EmbeddedMontiArcLanguageParser().parseModelOrThrowException("wrong path"));
    }

    @Test
    void parsingModels() throws IOException {
        final ASTEMACompilationUnit parsedModel = (ASTEMACompilationUnit) new EmbeddedMontiArcLanguageParser().parseModelOrThrowException("src/test/resources/models/pipeline/LinearPipeline.ema");
        assertEquals("LinearPipeline", parsedModel.getComponent().getName());
    }
}