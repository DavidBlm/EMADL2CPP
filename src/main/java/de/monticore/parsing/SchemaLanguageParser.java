package de.monticore.parsing;

import de.monticore.io.paths.ModelPath;
import schemalang._ast.ASTSchemaLangCompilationUnit;
import schemalang._parser.SchemaLangParser;

import java.io.IOException;
import java.util.Optional;

public class SchemaLanguageParser extends AbstractParser {

    public ASTSchemaLangCompilationUnit parseModel(final ModelPath pathToModel) throws IOException {
        final SchemaLangParser schemaLangParser = new SchemaLangParser();
        final Optional<ASTSchemaLangCompilationUnit> astSchemaLangCompilationUnit = schemaLangParser.parse("");
        return astSchemaLangCompilationUnit.orElseThrow(IllegalStateException::new);
    }

}
