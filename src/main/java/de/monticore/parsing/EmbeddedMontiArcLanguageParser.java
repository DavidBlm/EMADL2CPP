package de.monticore.parsing;

import de.monticore.lang.embeddedmontiarc.embeddedmontiarc._ast.ASTEMACompilationUnit;
import de.monticore.lang.embeddedmontiarc.embeddedmontiarc._parser.EmbeddedMontiArcParser;

import java.io.IOException;
import java.util.Optional;

public class EmbeddedMontiArcLanguageParser extends AbstractParser {

    public Optional<ASTEMACompilationUnit> parseModel(final String pathToModel) throws IOException {
        final EmbeddedMontiArcParser embeddedMontiArcParser = new EmbeddedMontiArcParser();
        return embeddedMontiArcParser.parse(pathToModel);
    }

}
