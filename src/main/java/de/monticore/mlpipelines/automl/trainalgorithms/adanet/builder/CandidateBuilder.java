package de.monticore.mlpipelines.automl.trainalgorithms.adanet.builder;

import de.monticore.lang.monticar.cnnarch._ast.ASTArchitectureElement;
import de.monticore.lang.monticar.cnnarch._ast.ASTStream;
import de.monticore.lang.monticar.cnnarch._symboltable.ArchitectureElementSymbol;
import de.monticore.lang.monticar.cnnarch._symboltable.ArchitectureSymbol;
import de.monticore.lang.monticar.cnnarch._symboltable.SerialCompositeElementSymbol;
import de.monticore.lang.monticar.cnnarch._symboltable.StreamInstructionSymbol;
import de.monticore.mlpipelines.automl.configuration.AdaNetConfig;
import de.monticore.mlpipelines.automl.trainalgorithms.adanet.models.AdaNetCandidate;

import java.util.List;

public class CandidateBuilder {
    AdaNetCandidate candidate;
    ArchitectureSymbol architecture;
    ArchitectureElementSymbol adaNetSymbol;
    ASTArchitectureElement adaNetAST;

    public CandidateBuilder() {
    }


    public ArchitectureSymbol build(AdaNetCandidate candidate, ArchitectureSymbol architecture) {
        this.candidate = candidate;
        this.architecture = architecture;

        buildSymbol();
        buildAST();
        setCandidateInArchitecture(adaNetSymbol);

        return null;
    }

    private void buildSymbol() {
        CandidateSymbolBuilder symbolBuilder = new CandidateSymbolBuilder();
        adaNetSymbol = symbolBuilder.build(candidate);
    }

    private void buildAST() {
        CandidateASTBuilder astBuilder = new CandidateASTBuilder();
        adaNetAST = astBuilder.build(candidate, adaNetSymbol);
    }

    private void setCandidateInArchitecture(ArchitectureElementSymbol adaNetSymbol) {
        SerialCompositeElementSymbol body = getNetworkInstructionBody();
        List<ArchitectureElementSymbol> symbolElements = body.getElements();
        ASTStream astNode = (ASTStream) body.getAstNode().get();
        List<ASTArchitectureElement> astElements = astNode.getElementsList();

        try {
            int adanetElementIndex = getAdaNetElementIndex(symbolElements);
            symbolElements.set(adanetElementIndex, adaNetSymbol);
            astElements.set(adanetElementIndex, adaNetAST);
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }

    private SerialCompositeElementSymbol getNetworkInstructionBody() {
        StreamInstructionSymbol networkInstructionSymbol =
                (StreamInstructionSymbol) architecture.getNetworkInstructions().get(0);
        SerialCompositeElementSymbol body = networkInstructionSymbol.getBody();
        return body;
    }

    private static int getAdaNetElementIndex(List<ArchitectureElementSymbol> elements) {
        for (int i = 0; i < elements.size(); i++) {
            if (isAdaNetSymbol(elements.get(i))) {
                return i;
            }
        }
        throw new RuntimeException("No AdaNet Symbol found in Architecture");
    }

    private static boolean isAdaNetSymbol(ArchitectureElementSymbol symbol) {
        return symbol.getName().equals(AdaNetConfig.ADA_NET_NAME);
    }
}
