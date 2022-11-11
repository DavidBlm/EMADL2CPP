package de.monticore.mlpipelines.automl.emadlprinter;

import de.monticore.lang.monticar.cnnarch._ast.*;
import de.monticore.lang.monticar.cnnarch._symboltable.ArchitectureSymbol;
import de.monticore.lang.monticar.cnnarch._visitor.CNNArchVisitor;
import de.monticore.prettyprint.AstPrettyPrinter;
import de.monticore.prettyprint.IndentPrinter;

import java.util.List;

public class EmadlPrettyPrinter implements AstPrettyPrinter<ASTArchitecture>, CNNArchVisitor {
    protected final IndentPrinter printer;

    public EmadlPrettyPrinter() {
        this.printer = new IndentPrinter();
        this.printer.setIndentLength(4);
    }

    public String prettyPrint(ArchitectureSymbol arch) {
        ASTArchitecture ast = (ASTArchitecture) arch.getAstNode().get();
        return prettyPrint(ast);
    }

    @Override
    public String prettyPrint(ASTArchitecture node) {
        this.printer.clearBuffer();
        this.handle(node);
        return this.printer.getContent();
    }

    @Override
    public void setRealThis(CNNArchVisitor realThis) {
        CNNArchVisitor.super.setRealThis(realThis);
    }

    @Override
    public CNNArchVisitor getRealThis() {
        return CNNArchVisitor.super.getRealThis();
    }

    @Override
    public void visit(ASTLayerDeclaration node) {
        String layerName = node.getName();
        this.printer.print("def " + layerName + "(");
        printASTLayerParams(node.getParametersList());
        this.printer.println("){");
        this.printer.indent();
        node.getBody().accept(this);
    }

    private void printASTLayerParams(List<ASTLayerParameter> params) {
        for (int i = 0; i < params.size(); i++) {
            params.get(i).accept(getRealThis());
            if (i < params.size() - 1) {
                this.printer.print(", ");
            }
        }
    }

    @Override
    public void traverse(ASTLayerDeclaration node) {

    }

    @Override
    public void endVisit(ASTLayerDeclaration node) {
        this.printer.unindent();
        this.printer.println("}");
        this.printer.println();
    }

//    @Override
//    public void handle(ASTLayer node) {
//    }

    @Override
    public void visit(ASTStream node) {
        List<ASTArchitectureElement> elements = node.getElementsList();
        for (int i = 0; i < elements.size(); i++) {
            ASTArchitectureElement element = elements.get(i);
            printASTArchitectureElement(element);
            if (i < elements.size() - 1) {
                printer.print(" ->");
            }
            printer.println();
        }
    }

    private void printASTArchitectureElement(ASTArchitectureElement element) {
        if (element instanceof ASTLayer)
            printASTLayer((ASTLayer) element);
    }

    public void printASTLayer(ASTLayer node) {
        String layerName = node.getName();
        this.printer.print(layerName + "(");
        List<ASTArchArgument> arguments = node.getArgumentsList();
        for (int i = 0; i < arguments.size(); i++) {
            printASTArchArgument(arguments.get(i));
            if (i < arguments.size() - 1) {
                this.printer.print(", ");
            }
        }
        this.printer.print(")");
    }

    @Override
    public void visit(ASTLayerParameter node) {
        String parameterName = node.getName();
        this.printer.print(parameterName);
        if (node.isPresentDefault()) {
            node.getDefault().accept(getRealThis());
        }
    }

    @Override
    public void endVisit(ASTParallelBlock node) {
        CNNArchVisitor.super.endVisit(node);
    }

    private void printASTArchArgument(ASTArchArgument argument) {
        if (argument instanceof ASTArchParameterArgument)
            printASTArchParameterArgument((ASTArchParameterArgument) argument);
        else {
            printer.print(argument.getName());
        }
    }

    public void printASTArchParameterArgument(ASTArchParameterArgument node) {
        String argumentName = node.getName();
        this.printer.print(argumentName + " = ");
        node.getRhs().accept(getRealThis());
    }

//    @Override
//    public void visit(ASTArchExpression node) {
//        node.getExpression().accept(getRealThis());
//    }
//
//    @Override
//    public void visit(ASTArchSimpleExpression node) {
//        node.getTupleExpression().accept(getRealThis());
//    }
//
//    @Override
//    public void visit(ASTTupleExpression node) {
//        for (int i = 0; i < node.getExpressionsList().size(); i++) {
//            node.getExpressionsList().get(i).accept(getRealThis());
//            if(i < node.getExpressionsList().size() - 1) {
//                this.printer.print(", ");
//            }
//        }
//    }
//
//    @Override
//    public void visit(ASTArchSimpleArithmeticExpression node) {
//        node.getNumberExpression().accept(getRealThis());
//    }
//
//    @Override
//    public void visit(ASTNumberExpression node) {
//        node.getNumberWithUnit().accept(getRealThis());
//    }
//
//    @Override
//    public void visit(ASTNumberWithUnit node) {
//        node.getNum().accept(getRealThis());
//    }

//    @Override
//    public void visit(ASTIntLiteral node) {
//        this.printer.print(node.getValue());
//    }
}
