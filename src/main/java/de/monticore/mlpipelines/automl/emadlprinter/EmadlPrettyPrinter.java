package de.monticore.mlpipelines.automl.emadlprinter;

import de.monticore.lang.monticar.cnnarch._ast.ASTArchitecture;
import de.monticore.lang.monticar.cnnarch._ast.ASTLayerDeclaration;
import de.monticore.lang.monticar.cnnarch._symboltable.ArchitectureSymbol;
import de.monticore.lang.monticar.cnnarch._visitor.CNNArchVisitor;
import de.monticore.prettyprint.AstPrettyPrinter;
import de.monticore.prettyprint.IndentPrinter;

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
    public void visit(ASTLayerDeclaration node) {
        String layerName = node.getName();
        this.printer.print("def " + layerName + "(");
    }

    @Override
    public void endVisit(ASTLayerDeclaration node) {
        this.printer.println("}");
        this.printer.println();
    }

    @Override
    public void traverse(ASTLayerDeclaration node) {
        this.printer.println("){");
    }
}
