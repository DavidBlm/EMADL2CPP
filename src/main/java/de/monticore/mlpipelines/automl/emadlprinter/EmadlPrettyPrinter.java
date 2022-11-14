package de.monticore.mlpipelines.automl.emadlprinter;

import de.monticore.lang.monticar.cnnarch._ast.*;
import de.monticore.lang.monticar.cnnarch._symboltable.ArchitectureSymbol;
import de.monticore.lang.monticar.cnnarch._visitor.CNNArchVisitor;
import de.monticore.prettyprint.AstPrettyPrinter;
import de.monticore.prettyprint.IndentPrinter;

import java.util.List;
import java.util.Optional;

public class EmadlPrettyPrinter implements AstPrettyPrinter<ASTArchitecture>, CNNArchVisitor {
    protected final IndentPrinter printer;

    public EmadlPrettyPrinter() {
        this.printer = new IndentPrinter();
        this.printer.setIndentLength(4);
    }

    public String prettyPrint(ArchitectureSymbol arch) {
        Optional<String> nameOptional = arch.getEnclosingScope().getEnclosingScope().get().getName();
        String archName = nameOptional.orElse("unnamed");

        this.printer.clearBuffer();
        this.printer.println("component " + archName + "{");
        this.printer.indent();
        new PortPrinter(this.printer).printPorts(arch);
        this.printer.println("");
        this.printer.println("implementation CNN {");
        this.printer.indent();
        this.handle((ASTArchitecture) arch.getAstNode().get());
        this.printer.unindent();
        this.printer.println("}");
        this.printer.unindent();
        this.printer.println("}");
        return this.printer.getContent();
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
        printASTStream(node.getBody());
    }

    private void printASTLayerParams(List<ASTLayerParameter> params) {
        for (int i = 0; i < params.size(); i++) {
            String name = params.get(i).getName();
            this.printer.print(name);
            if (i < params.size() - 1) {
                this.printer.print(", ");
            }
        }
    }

    @Override
    public void endVisit(ASTLayerDeclaration node) {
        this.printer.unindent();
        this.printer.println("}");
        this.printer.println();
    }

    public void printASTStream(ASTStream node) {
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
        else if (element instanceof ASTParallelBlock)
            printASTParallelBlock((ASTParallelBlock) element);
        else if (element instanceof ASTVariable)
            printASTVariable((ASTVariable) element);
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

    private void printASTParallelBlock(ASTParallelBlock element) {
        printer.println("(");
        List<ASTStream> groups = element.getGroupsList();
        for (int i = 0; i < groups.size(); i++) {
            printer.indent();
            printASTStream(groups.get(i));
            printer.unindent();
            if (i < groups.size() - 1) {
                printer.println("| ");
            }
        }
        printer.print(")");
    }

    private void printASTVariable(ASTVariable element) {
        printer.print(element.getName());
    }

    private void printASTArchArgument(ASTArchArgument argument) {
        if (argument instanceof ASTArchParameterArgument)
            printASTArchParameterArgument((ASTArchParameterArgument) argument);
        else if(argument instanceof ASTArchSpecialArgument)
            printASTArchSpecialArgument((ASTArchSpecialArgument) argument);
        //else if(argument instanceof ASTArchValueArgument)
        //    printASTArchValueArgument((ASTArchValueArgument) argument);
        else {
            printer.print(argument.getName());
        }
    }

    private void printASTArchSpecialArgument(ASTArchSpecialArgument argument) {
        String argumentName = argument.getName();
        this.printer.print(argumentName + " = ");
        NumberPrinter numberPrinter = new NumberPrinter(printer);
        numberPrinter.printASTArchExpression(argument.getRhs());
    }

    public void printASTArchParameterArgument(ASTArchParameterArgument node) {
        String argumentName = node.getName();
        this.printer.print(argumentName + " = ");
        NumberPrinter numberPrinter = new NumberPrinter(printer);
        numberPrinter.printASTArchExpression(node.getRhs());
    }

    @Override
    public void visit(ASTStreamInstruction node) {
        printASTStream(node.getBody());
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
