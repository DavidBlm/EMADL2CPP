package de.monticore.mlpipelines.automl.trainalgorithms.adanet;

import de.monticore.lang.monticar.cnnarch._ast.ASTArchitecture;

public class CandidateASTNodeBuilder {
    public ASTArchitecture build(ASTArchitecture refArch, AdaNetCandidate candidate) {
        ASTArchitecture newArch = refArch.deepClone();

        return newArch;
    }
}
