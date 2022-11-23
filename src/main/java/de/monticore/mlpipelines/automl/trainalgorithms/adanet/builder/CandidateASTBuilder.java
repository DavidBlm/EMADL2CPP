package de.monticore.mlpipelines.automl.trainalgorithms.adanet.builder;

import de.monticore.lang.monticar.cnnarch._ast.*;
import de.monticore.lang.monticar.cnnarch._symboltable.*;

import java.util.List;

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
                ParallelCompositeElementSymbol castedElement = (ParallelCompositeElementSymbol) element;
                ASTParallelBlock elementGroup = createASTParallelBlockForSymbol(castedElement);
                builder.addElements(elementGroup);
            } else if (element instanceof LayerSymbol) {
                LayerSymbol castedElement = (LayerSymbol) element;
                ASTLayer layer = createLayerForElement(castedElement);
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
        builder.setName(element.getName());

        List<ArgumentSymbol> arguments = element.getArguments();
        if (arguments != null) {
            for (ArgumentSymbol argumentSymbol : arguments) {
//            ASTArchArgument argumentAst = createArgument(argumentSymbol);
//            builder.addArguments(argumentAst);
            }
        }
        ASTLayer ast = builder.build();
        element.setAstNode(ast);
        return ast;
    }

//    private static ASTArchArgument createArgument(ArgumentSymbol symbol) {
//        ASTArchParameterArgumentBuilder builder = CNNArchMill.archParameterArgumentBuilder();
//        builder.setSymbol(symbol);
//        ASTArchArgument ast = builder.build();
//        symbol.setAstNode(ast);
//        return ast;
//    }
}
