package de.monticore.mlpipelines.automl.trainalgorithms.adanet;

import de.monticore.lang.monticar.cnnarch._symboltable.ArchitectureElementSymbol;
import de.monticore.lang.monticar.cnnarch._symboltable.LayerSymbol;
import de.monticore.lang.monticar.cnnarch._symboltable.ParallelCompositeElementSymbol;
import de.monticore.mlpipelines.automl.trainalgorithms.adanet.custom.models.LayerSymbolCustom;
import de.monticore.mlpipelines.automl.trainalgorithms.adanet.custom.models.ParallelCompositeElementSymbolCustom;
import de.monticore.mlpipelines.automl.trainalgorithms.adanet.custom.models.SerialCompositeElementSymbolCustom;
import de.monticore.mlpipelines.automl.trainalgorithms.adanet.models.AdaNetCandidate;
import de.monticore.mlpipelines.automl.trainalgorithms.adanet.models.ParallelCandidateLayer;
import de.monticore.mlpipelines.automl.trainalgorithms.adanet.models.ParallelCandidateLayerElement;

import java.util.ArrayList;
import java.util.List;

public class CandidateBuilder {
    AdaNetCandidate candidate;

    public ArchitectureElementSymbol build(AdaNetCandidate candidate) {
        this.candidate = candidate;
        return getCandidateSymbol();
    }

//    private List<ASTArchitectureElement> getAstArchitectureElements(ASTArchitecture newArch) {
//        ASTInstruction instruction = newArch.getInstructions(0);
//        ASTStreamInstruction streamInstruction = (ASTStreamInstruction) instruction.getNetworkInstruction();
//        ASTStream stream = streamInstruction.getBody();
//        List<ASTArchitectureElement> elements = stream.getElementsList();
//        return elements;
//    }
//
//    private void replaceAdaNetKeyWordByCandidate(List<ASTArchitectureElement> elements) {
//        for (int i = 0; i < elements.size(); i++) {
//            ASTArchitectureElement element = elements.get(i);
//            if (element instanceof ASTLayer) {
//                ASTLayer layer = (ASTLayer) element;
//                if (layer.getName().equals("AdaNet")) {
//                    elements.set(i, getCandidateSymbol());
//                }
//            }
//        }
//    }

    private ParallelCompositeElementSymbol getCandidateSymbol() {
        ParallelCompositeElementSymbolCustom candidateSymbol = new ParallelCompositeElementSymbolCustom();
        List<ArchitectureElementSymbol> parallelElements = getRootSerialCompositeElement();
        candidateSymbol.setElements(parallelElements);

        return candidateSymbol;
    }

    private List<ArchitectureElementSymbol> getRootSerialCompositeElement() {
        SerialCompositeElementSymbolCustom compositeElementSymbol = new SerialCompositeElementSymbolCustom();

        List<ArchitectureElementSymbol> parallelElements = new ArrayList<>();
        parallelElements.add(compositeElementSymbol);

        List<ArchitectureElementSymbol> serialLayers = getLayers();
        compositeElementSymbol.setElements(serialLayers);

        return parallelElements;
    }

    private List<ArchitectureElementSymbol> getLayers() {
        List<ArchitectureElementSymbol> serialElements = new ArrayList<>();
        for (ParallelCandidateLayer layer : this.candidate.getParallelCandidateLayers()) {
            ParallelCompositeElementSymbol parallelLayer = getParallelLayer(layer);
            serialElements.add(parallelLayer);
            serialElements.add(getConcatenateLayer());
        }
        return serialElements;
    }

    private ParallelCompositeElementSymbol getParallelLayer(ParallelCandidateLayer layer) {
        ParallelCompositeElementSymbolCustom parallelSymbol = new ParallelCompositeElementSymbolCustom();
        List<ArchitectureElementSymbol> elements = new ArrayList<>();
        for (ParallelCandidateLayerElement element : layer.getElements()) {
            SerialCompositeElementSymbolCustom parallelElement = getParallelLayerElement(element);
            elements.add(parallelElement);
        }
        parallelSymbol.setElements(elements);
        return parallelSymbol;
    }

    private LayerSymbol getConcatenateLayer() {
        LayerSymbolCustom layer = new LayerSymbolCustom("Concatenate");
        return layer;
    }

    private SerialCompositeElementSymbolCustom getParallelLayerElement(ParallelCandidateLayerElement element) {
        SerialCompositeElementSymbolCustom serialSymbol = new SerialCompositeElementSymbolCustom();
        LayerSymbol layer = getFullyConnectedLayer(element.getUnits());
        List<ArchitectureElementSymbol> elements = new ArrayList<>();
        elements.add(layer);
        serialSymbol.setElements(elements);
        return serialSymbol;
    }

    private LayerSymbol getFullyConnectedLayer(int units) {
        LayerSymbolCustom layer = new LayerSymbolCustom("FullyConnected");
        return layer;
    }
}
