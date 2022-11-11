package de.monticore.mlpipelines.automl.emadlprinter;

import de.monticore.lang.math._ast.ASTNumberExpression;
import de.monticore.lang.monticar.cnnarch._ast.*;
import de.monticore.literals.literals._ast.ASTIntLiteral;
import de.monticore.literals.literals._ast.ASTNumericLiteral;
import de.monticore.numberunit._ast.ASTNumberWithInf;
import de.monticore.numberunit._ast.ASTNumberWithUnit;
import de.monticore.prettyprint.IndentPrinter;

import java.util.List;
import java.util.Optional;

public class NumberPrinter {
    private final IndentPrinter printer;

    public NumberPrinter(IndentPrinter printer) {
        this.printer = printer;
    }


    public void printASTArchExpression(ASTArchExpression rhs) {
        ASTArchSimpleExpression expression = rhs.getExpression();
        printASTArchSimpleExpression(expression);
    }


    private void printASTArchSimpleExpression(ASTArchSimpleExpression expression) {
        Optional<ASTTupleExpression> tupleExpressionOpt = expression.getTupleExpressionOpt();
        if (tupleExpressionOpt.isPresent())
            printASTTupleExpression(tupleExpressionOpt.get());

        Optional<ASTArchArithmeticExpression> arithmeticExpression = expression.getArithmeticExpressionOpt();
        if (arithmeticExpression.isPresent()) {
            ASTArchArithmeticExpression astArchArithmeticExpression = arithmeticExpression.get();
            if (astArchArithmeticExpression instanceof ASTArchSimpleArithmeticExpression)
                printASTSimpleArithmeticExpression((ASTArchSimpleArithmeticExpression) arithmeticExpression.get());
        }

    }

    private void printASTTupleExpression(ASTTupleExpression astTupleExpression) {
        List<ASTArchArithmeticExpression> expressions = astTupleExpression.getExpressionsList();
        printer.print("(");
        for (int i = 0; i < expressions.size(); i++) {
            printASTArithmeticExpression(expressions.get(i));
            if (i < expressions.size() - 1) {
                printer.print(", ");
            }
        }
        printer.print(")");
    }

    private void printASTArithmeticExpression(ASTArchArithmeticExpression expression) {
        if (expression instanceof ASTArchSimpleArithmeticExpression)
            printASTSimpleArithmeticExpression((ASTArchSimpleArithmeticExpression) expression);
    }

    private void printASTSimpleArithmeticExpression(ASTArchSimpleArithmeticExpression expression) {
        Optional<ASTNumberExpression> numberExpressionOpt = expression.getNumberExpressionOpt();
        if (numberExpressionOpt.isPresent())
            printASTNumberExpression(numberExpressionOpt.get());
    }

    private void printASTNumberExpression(ASTNumberExpression expression) {
        ASTNumberWithUnit numberWithUnit = expression.getNumberWithUnit();
        printASTNumberWithUnit(numberWithUnit);
    }

    private void printASTNumberWithUnit(ASTNumberWithUnit numberWithUnit) {
        Optional<ASTNumberWithInf> numOpt = numberWithUnit.getNumOpt();
        if (numOpt.isPresent())
            printASTNumberWithInf(numOpt.get());
    }

    private void printASTNumberWithInf(ASTNumberWithInf num) {
        if (num.isPresentInf()) {
            printer.print("inf");
        } else {
            ASTNumericLiteral literal = num.getNumber();

            if (literal instanceof ASTIntLiteral) {
                ASTIntLiteral number = (ASTIntLiteral) literal;
                printer.print("" + number.getValue());
            }
        }
    }
}
