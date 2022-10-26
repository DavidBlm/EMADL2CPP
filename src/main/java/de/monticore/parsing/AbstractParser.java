package de.monticore.parsing;

import de.monticore.ast.ASTCNode;

import java.io.IOException;
import java.util.Optional;

//TODO check compliance with  MC 7
public abstract class AbstractParser {

    public ASTCNode parseModelOrThrowException(final String pathToModel) throws IOException {

        return parseModel(pathToModel).orElseThrow(IllegalStateException::new);
    }

    abstract public Optional<? extends ASTCNode> parseModel(final String pathToModel) throws IOException;
}
