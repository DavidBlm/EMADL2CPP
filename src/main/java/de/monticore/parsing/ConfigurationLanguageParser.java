package de.monticore.parsing;

import conflang._ast.ASTConfLangCompilationUnit;
import de.monticore.io.paths.ModelPath;

import java.io.IOException;
import java.util.Optional;

public class ConfigurationLanguageParser extends AbstractParser{

    public ASTConfLangCompilationUnit parseModel(final ModelPath pathToModel) throws IOException {
        final conflang._parser.ConfLangParser confLangParser = new conflang._parser.ConfLangParser();
        final Optional<ASTConfLangCompilationUnit> astConfLangCompilationUnit = confLangParser.parse("");
        return astConfLangCompilationUnit.orElseThrow(IllegalStateException::new);
    }


}
