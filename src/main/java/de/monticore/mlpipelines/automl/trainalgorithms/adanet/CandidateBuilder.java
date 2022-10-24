package de.monticore.mlpipelines.automl.trainalgorithms.adanet;

import com.rits.cloning.Cloner;
import de.monticore.lang.monticar.cnnarch._symboltable.ArchitectureSymbol;
import de.monticore.mlpipelines.ModelLoader;

public class CandidateBuilder {
    private ArchitectureSymbol startArchitecture;
    private AdaNetComponent component;
    private Cloner cloner;


    public CandidateBuilder() {
    }

    public ArchitectureSymbol createCandidate(AdaNetComponent component) {
        this.component = component;
        this.startArchitecture = ModelLoader.loadAdaNet();

        return startArchitecture;
    }
}
