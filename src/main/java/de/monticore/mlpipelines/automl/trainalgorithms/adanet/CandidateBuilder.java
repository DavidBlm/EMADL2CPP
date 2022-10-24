package de.monticore.mlpipelines.automl.trainalgorithms.adanet;

import de.monticore.lang.monticar.cnnarch._symboltable.ArchitectureSymbol;
import de.monticore.lang.monticar.cnnarch._symboltable.NetworkInstructionSymbol;
import de.monticore.lang.monticar.cnnarch._symboltable.ParallelCompositeElementSymbol;
import de.monticore.lang.monticar.cnnarch._symboltable.SerialCompositeElementSymbol;
import de.monticore.mlpipelines.ModelLoader;

import java.util.ArrayList;
import java.util.List;

public class CandidateBuilder {
    private ArchitectureSymbol architecture;


    public CandidateBuilder() {
    }

    public ArchitectureSymbol createCandidate(List<AdaNetComponent> components) {
        loadStartArchitecture();
        addComponentsToArchitecture(components);

        return architecture;
    }

    private ArchitectureSymbol loadStartArchitecture() {
        architecture = ModelLoader.loadAdaNet();
        clearParallelLayersList();
        return architecture;
    }

    private void addComponentsToArchitecture(List<AdaNetComponent> components) {
        for (AdaNetComponent component : components) {
            addComponentToArchitecture(component);
        }
    }

    private void clearParallelLayersList() {
        NetworkInstructionSymbol networkInstruction = architecture.getNetworkInstructions().get(0);
        ArrayList elements = (ArrayList) networkInstruction.getBody().getElements();
        ParallelCompositeElementSymbol parallelBlock = (ParallelCompositeElementSymbol) elements.get(1);
        List parallelLayers = parallelBlock.getElements();
        parallelLayers.clear();
    }

    private void addComponentToArchitecture(AdaNetComponent component) {
        SerialCompositeElementSymbol serialBlock = new SerialCompositeElementSymbol();
        ArrayList elements = (ArrayList) serialBlock.getElements();
        for (int i = 0; i < component.getNumberLayers(); i++) {
            String layerName = "layer" + i;

//            LayerSymbol layer = new LayerSymbol(layerName);
        }
    }
}
