package de.monticore.mlpipelines.automl.trainalgorithms.adanet;

import de.monticore.ast.ASTNode;
import de.monticore.lang.monticar.cnnarch._symboltable.*;

import java.util.ArrayList;
import java.util.List;

public class CandidateBuilder {
    private ArchitectureSymbol architecture;
    private ASTNode astNode;


    public CandidateBuilder() {
    }

    public ArchitectureSymbol createCandidate(List<AdaNetComponent> components) {
        loadStartArchitecture();
        addComponentsToArchitecture(components);

        return architecture;
    }

    private void loadStartArchitecture() {

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
        List<ArchitectureElementSymbol> parallelLayers = parallelBlock.getElements();

        parallelLayers.clear();
    }

    private void addComponentToArchitecture(AdaNetComponent component) {
        SerialCompositeElementSymbol serialBlock = new SerialCompositeElementSymbol();
        ArrayList elements = (ArrayList) serialBlock.getElements();
        for (int i = 0; i < component.getNumberLayers(); i++) {
            String layerName = "layer" + i;

//            List<ArgumentSymbol> arguments = getArguments(component);
//            LayerSymbol layer = new LayerSymbol.Builder().arguments(arguments).build();
        }
    }
}
