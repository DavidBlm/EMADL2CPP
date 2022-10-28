package de.monticore.parsing;

import de.monticore.lang.embeddedmontiarc.embeddedmontiarc._ast.ASTEMACompilationUnit;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EMADLTest {

    @Test
    void exceptionByParsing() {
        assertThrows(IOException.class, () -> new EMADLParser().parseModelOrThrowException("wrong path"));
    }

    @Test
    void parsingEMA() throws IOException {
        final ASTEMACompilationUnit parsedModel = (ASTEMACompilationUnit) new EMADLParser().parseModelOrThrowException("src/test/resources/models/pipeline/LinearPipeline.ema");
        assertEquals("LinearPipeline", parsedModel.getComponent().getName());
    }

    @Test
    void parsingEMADL() throws IOException {
        final ASTEMACompilationUnit parsedModel = (ASTEMACompilationUnit) new EMADLParser().parseModelOrThrowException("src/test/resources/models/Add.emadl");
        assertEquals("Add", parsedModel.getComponent().getName());
    }
}