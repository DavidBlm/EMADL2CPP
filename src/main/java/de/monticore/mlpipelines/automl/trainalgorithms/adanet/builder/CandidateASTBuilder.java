package de.monticore.mlpipelines.automl.trainalgorithms.adanet.builder;

import de.monticore.lang.monticar.cnnarch._ast.ASTParallelBlock;
import de.monticore.lang.monticar.cnnarch._symboltable.ArchitectureElementSymbol;
import de.monticore.mlpipelines.automl.trainalgorithms.adanet.models.AdaNetCandidate;

public class CandidateASTBuilder {
    AdaNetCandidate candidate;
    ArchitectureElementSymbol adanetSymbol;

    public ASTParallelBlock build(AdaNetCandidate candidate, ArchitectureElementSymbol adanetSymbol) {
        this.candidate = candidate;
        this.adanetSymbol = adanetSymbol;

        return null;
    }
}
