package de.monticore.mlpipelines.automl.trainalgorithms.adanet;

import com.rits.cloning.Cloner;
import de.monticore.lang.monticar.cnnarch._symboltable.ArchitectureSymbol;

public class CandidateBuilder {
    private ArchitectureSymbol architecture;
    private AdaNetComponent component;
    private Cloner cloner;


    public CandidateBuilder() {
        cloner = new Cloner();
    }

    public ArchitectureSymbol createCandidate(ArchitectureSymbol architecture, AdaNetComponent component) {
        this.architecture = cloner.shallowClone(architecture);
        this.component = component;

        return architecture;
    }
}
