package de.monticore.mlpipelines.automl.trainalgorithms.adanet;

import de.monticore.lang.monticar.cnnarch._symboltable.ArchitectureSymbol;
import de.monticore.mlpipelines.helper.DeepObjectCloner;

public class CandidateBuilder {
    private ArchitectureSymbol architecture;
    private AdaNetComponent component;
    private DeepObjectCloner<ArchitectureSymbol> deepObjectCloner;


    public CandidateBuilder() {
        deepObjectCloner = new DeepObjectCloner();
    }

    public ArchitectureSymbol createCandidate(ArchitectureSymbol architecture, AdaNetComponent component) {
        this.architecture = deepObjectCloner.clone(architecture);
        this.component = component;

        return architecture;
    }
}
