package de.monticore.mlpipelines.automl.trainalgorithms.adanet.builder;

import de.monticore.lang.math._ast.ASTNumberExpression;
import de.monticore.lang.math._ast.ASTNumberExpressionBuilder;
import de.monticore.lang.math._ast.MathMill;
import de.monticore.lang.math._symboltable.expression.MathExpressionSymbol;
import de.monticore.lang.math._symboltable.expression.MathNumberExpressionSymbol;
import de.monticore.lang.monticar.cnnarch._ast.*;
import de.monticore.lang.monticar.cnnarch._symboltable.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
        List<ASTArchArgument> argumentAsts = getAstArchArguments(element);
        builder.addAllArgumentss(argumentAsts);

        ASTLayer ast = builder.build();
        element.setAstNode(ast);
        return ast;
    }

    private List<ASTArchArgument> getAstArchArguments(LayerSymbol element) {
        List<ASTArchArgument> argumentAsts = new ArrayList<>();
        List<ArgumentSymbol> argumentSymbols = element.getArguments();
        if (argumentSymbols != null) {
            for (ArgumentSymbol argumentSymbol : argumentSymbols) {
                ASTArchParameterArgument argumentAst = createArgument(argumentSymbol);
                argumentAsts.add(argumentAst);
            }
        }
        return argumentAsts;
    }

    private ASTArchParameterArgument createArgument(ArgumentSymbol symbol) {
        ASTArchParameterArgumentBuilder builder = CNNArchMill.archParameterArgumentBuilder();
        builder.setName(symbol.getName());
        builder.setSymbol(symbol);
        ASTArchExpression archExpression = createArchExpression(symbol.getRhs());
        builder.setRhs(archExpression);

        ASTArchParameterArgument ast = builder.build();
        symbol.setAstNode(ast);
        return ast;
    }

    private ASTArchExpression createArchExpression(ArchExpressionSymbol symbol) {
        if (symbol instanceof ArchSimpleExpressionSymbol) {
            ArchSimpleExpressionSymbol symbolCasted = (ArchSimpleExpressionSymbol) symbol;
            ASTArchExpressionBuilder builder = CNNArchMill.archExpressionBuilder();
            builder.setSymbol(symbol);
            Optional<MathExpressionSymbol> mathExpressionSymbol = symbolCasted.getMathExpression();
            ASTArchSimpleExpression mathExpression = createMathExpression(
                    ArchSimpleExpressionSymbol.of(mathExpressionSymbol.orElse(null)));
            builder.setExpression(mathExpression);
            ASTArchExpression ast = builder.build();
            symbol.setAstNode(ast);
            return ast;
        }

        throw new UnsupportedOperationException("This type of ArchExpressionSymbol is not supported yet.");
    }

    private ASTArchSimpleExpression createMathExpression(ArchSimpleExpressionSymbol symbol) {
        ASTArchSimpleExpressionBuilder builder = CNNArchMill.archSimpleExpressionBuilder();
        builder.setSymbol(symbol);
        MathNumberExpressionSymbol mathNumberExpressionSymbol = (MathNumberExpressionSymbol) symbol.getMathExpression()
                .orElse(null);
        ASTArchSimpleArithmeticExpression arithmeticExpression = createArithmeticExpression(mathNumberExpressionSymbol);
        builder.setArithmeticExpression(arithmeticExpression);
        ASTArchSimpleExpression ast = builder.build();
        symbol.setAstNode(ast);
        return ast;
    }

    private ASTArchSimpleArithmeticExpression createArithmeticExpression(MathNumberExpressionSymbol symbol) {
        ASTArchSimpleArithmeticExpressionBuilder builder = CNNArchMill.archSimpleArithmeticExpressionBuilder();
        builder.setSymbol(symbol);
        ASTNumberExpression numberExpression = createNumberExpression(symbol.getRe);
        builder.setExpression();
        ASTArchSimpleArithmeticExpression ast = builder.build();
        symbol.setAstNode(ast);
        return ast;
    }

    private ASTNumberExpression createNumberExpression(MathNumberExpressionSymbol symbol) {
        ASTNumberExpressionBuilder builder = MathMill.numberExpressionBuilder();
        builder.setSymbol(symbol);
        builder.setValue(symbol.getValue());
        ASTNumberExpression ast = builder.build();
        symbol.setAstNode(ast);
        return ast;
    }
}
