package de.monticore.parsing;

import de.monticore.ast.ASTCNode;
import de.monticore.io.paths.ModelPath;

import java.io.IOException;

public abstract class AbstractParser {
    //TODO switch to modelpath from MC 7
    abstract public ASTCNode parseModel(final ModelPath pathToModel) throws IOException;
}
