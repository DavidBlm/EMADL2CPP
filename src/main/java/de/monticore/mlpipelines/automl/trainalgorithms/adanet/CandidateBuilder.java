package de.monticore.mlpipelines.automl.trainalgorithms.adanet;

import de.monticore.lang.monticar.cnnarch._symboltable.ArchitectureSymbol;
import de.monticore.mlpipelines.ModelLoader;

import java.util.List;

public class CandidateBuilder {
    private ArchitectureSymbol architecture;


    public CandidateBuilder() {
    }

    public ArchitectureSymbol createCandidate(List<AdaNetComponent> components) {
        this.architecture = getStartArchitecture();
        addComponentsToArchitecture(components);

        return architecture;
    }

    private ArchitectureSymbol getStartArchitecture() {
        ArchitectureSymbol architecture = ModelLoader.loadAdaNet();
        addParallelBlockToArchitecture();
        return architecture;
    }

    private void addComponentsToArchitecture(List<AdaNetComponent> components) {
        for (AdaNetComponent component : components) {
            addComponentToArchitecture(component);
        }
    }

    private void addParallelBlockToArchitecture() {
    }

    private void addComponentToArchitecture(AdaNetComponent component) {

    }
}
