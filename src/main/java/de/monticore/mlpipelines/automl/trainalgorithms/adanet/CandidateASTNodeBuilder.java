package de.monticore.mlpipelines.automl.trainalgorithms.adanet;

import de.monticore.lang.monticar.cnnarch._ast.*;
import de.monticore.mlpipelines.automl.trainalgorithms.adanet.builder.ASTLayerBuilderCustom;
import de.monticore.mlpipelines.automl.trainalgorithms.adanet.builder.ASTParallelBlockBuilderCustom;
import de.monticore.mlpipelines.automl.trainalgorithms.adanet.builder.ASTStreamBuilderCustom;
import de.monticore.mlpipelines.automl.trainalgorithms.adanet.models.AdaNetCandidate;
import de.monticore.mlpipelines.automl.trainalgorithms.adanet.models.ParallelCandidateLayer;
import de.monticore.mlpipelines.automl.trainalgorithms.adanet.models.ParallelCandidateLayerElement;

import java.util.List;

public class CandidateASTNodeBuilder {
    AdaNetCandidate candidate;

    public ASTArchitecture build(ASTArchitecture refArch, AdaNetCandidate candidate) {
        this.candidate = candidate;

        ASTArchitecture newArch = refArch.deepClone();
        List<ASTArchitectureElement> elements = getAstArchitectureElements(newArch);
        replaceAdaNetKeyWordByCandidate(elements);
        return newArch;
    }

    private List<ASTArchitectureElement> getAstArchitectureElements(ASTArchitecture newArch) {
        ASTInstruction instruction = newArch.getInstructions(0);
        ASTStreamInstruction streamInstruction = (ASTStreamInstruction) instruction.getNetworkInstruction();
        ASTStream stream = streamInstruction.getBody();
        List<ASTArchitectureElement> elements = stream.getElementsList();
        return elements;
    }

    private void replaceAdaNetKeyWordByCandidate(List<ASTArchitectureElement> elements) {
        for (int i = 0; i < elements.size(); i++) {
            ASTArchitectureElement element = elements.get(i);
            if (element instanceof ASTLayer) {
                ASTLayer layer = (ASTLayer) element;
                if (layer.getName().equals("AdaNet")) {
                    elements.set(i, getCandidate());
                }
            }
        }
    }

    private ASTParallelBlock getCandidate() {
        ASTParallelBlockBuilder candidateBuilder = new ASTParallelBlockBuilderCustom();
        ASTStreamBuilder candidateLayersBuilder = new ASTStreamBuilderCustom();
        for (ParallelCandidateLayer layer : this.candidate.getParallelCandidateLayers()) {
            ASTParallelBlock parallelLayer = getParallelLayer(layer);
            candidateLayersBuilder.addElements(parallelLayer);
            candidateLayersBuilder.addElements(getConcatenateLayer());
        }
        ASTStream candidateLayers = candidateLayersBuilder.build();
        candidateBuilder.addGroups(candidateLayers);
        return candidateBuilder.build();
    }

    private ASTParallelBlock getParallelLayer(ParallelCandidateLayer layer) {
        ASTParallelBlockBuilder parallelBlockBuilder = new ASTParallelBlockBuilderCustom();
        for (ParallelCandidateLayerElement element : layer.getElements()) {
            ASTStream parallelElement = getLayerElementAstStream(element);
            parallelBlockBuilder.addGroups(parallelElement);
        }
        return parallelBlockBuilder.build();
    }

    private ASTLayer getConcatenateLayer() {
        ASTLayerBuilder layerBuilder = new ASTLayerBuilderCustom();
        layerBuilder.setName("Concatenate");
        ASTLayer layer = layerBuilder.build();
        return layer;
    }

    private ASTStream getLayerElementAstStream(ParallelCandidateLayerElement element) {
        ASTStreamBuilder streamBuilder = new ASTStreamBuilderCustom();
        ASTLayer layer = getAstLayer(element.getUnits());
        streamBuilder.addElements(layer);
        return streamBuilder.build();
    }

    private ASTLayer getAstLayer(int units) {
        ASTLayerBuilder layerBuilder = new ASTLayerBuilderCustom();
        layerBuilder.setName("FullyConnected");
        ASTLayer layer = layerBuilder.build();
        return layer;
    }
}
