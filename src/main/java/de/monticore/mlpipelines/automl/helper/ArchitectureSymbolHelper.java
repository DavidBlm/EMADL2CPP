package de.monticore.mlpipelines.automl.helper;

import de.monticore.lang.embeddedmontiarc.embeddedmontiarc._symboltable.instanceStructure.EMAPortInstanceSymbol;
import de.monticore.lang.monticar.cnnarch._symboltable.ArchitectureSymbol;
import de.monticore.lang.monticar.common2._ast.ASTCommonMatrixType;
import de.monticore.lang.monticar.ts.references.MCASTTypeSymbolReference;
import de.monticore.lang.monticar.types2._ast.ASTDimension;
import de.monticore.symboltable.Symbol;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public class ArchitectureSymbolHelper {

    public static ASTDimension getImageDimension(ArchitectureSymbol architecture) {
        Map<String, Collection<Symbol>> enclosedSymbols = architecture.getSpannedScope()
                .getEnclosingScope()
                .get()
                .getLocalSymbols();
        ArrayList image = (ArrayList) enclosedSymbols.get("image");
        EMAPortInstanceSymbol instanceSymbols = (EMAPortInstanceSymbol) image.get(0);
        MCASTTypeSymbolReference typeReference = (MCASTTypeSymbolReference) instanceSymbols.getTypeReference();
        ASTCommonMatrixType astType = (ASTCommonMatrixType) typeReference.getAstType();
        ASTDimension dimensions = astType.getDimension();
        return dimensions;
    }
}
