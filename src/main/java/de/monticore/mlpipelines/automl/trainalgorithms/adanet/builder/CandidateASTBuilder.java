package de.monticore.mlpipelines.automl.trainalgorithms.adanet.builder;

import de.monticore.lang.monticar.cnnarch._ast.*;
import de.monticore.lang.monticar.cnnarch._symboltable.ArchitectureElementSymbol;
import de.monticore.lang.monticar.cnnarch._symboltable.LayerSymbol;
import de.monticore.lang.monticar.cnnarch._symboltable.ParallelCompositeElementSymbol;
import de.monticore.lang.monticar.cnnarch._symboltable.SerialCompositeElementSymbol;

public class CandidateASTBuilder {
    ParallelCompositeElementSymbol adanetSymbol;

    public ASTParallelBlock build(ParallelCompositeElementSymbol adanetSymbol) {
        this.adanetSymbol = adanetSymbol;
        return buildParallelBlock();
    }

    private ASTParallelBlock buildParallelBlock() {
        return createASTParallelBlockForSymbol(adanetSymbol);
    }

    private ASTParallelBlock createASTParallelBlockForSymbol(ParallelCompositeElementSymbol element) {
        ASTParallelBlockBuilder builder = CNNArchMill.parallelBlockBuilder();
        builder.setSymbol(element);
        for (ArchitectureElementSymbol elementSymbol : element.getElements()) {
            if (elementSymbol instanceof SerialCompositeElementSymbol) {
                ASTStream elementGroup = createASTStreamForSymbol((SerialCompositeElementSymbol) elementSymbol);
                builder.addGroups(elementGroup);
            }
        }
        ASTParallelBlock ast = builder.build();
        element.setAstNode(ast);
        return ast;
    }

    private ASTStream createASTStreamForSymbol(SerialCompositeElementSymbol symbol) {
        ASTStreamBuilder builder = CNNArchMill.streamBuilder();
        builder.setSymbol(symbol);
        for (ArchitectureElementSymbol element : symbol.getElements()) {
            if (element instanceof ParallelCompositeElementSymbol) {
                ASTParallelBlock elementGroup = createASTParallelBlockForSymbol(
                        (ParallelCompositeElementSymbol) element);
                builder.addElements(elementGroup);
            } else if (element instanceof LayerSymbol) {
                ASTLayer layer = createLayerForElement((LayerSymbol) element);
                builder.addElements(layer);
            }
        }
        ASTStream ast = builder.build();
        symbol.setAstNode(ast);
        return ast;
    }

    private ASTLayer createLayerForElement(LayerSymbol element) {
        ASTLayerBuilder builder = CNNArchMill.layerBuilder();
        builder.setSymbol(element);
        ASTLayer ast = builder.build();
        element.setAstNode(ast);
        return ast;
    }
}
