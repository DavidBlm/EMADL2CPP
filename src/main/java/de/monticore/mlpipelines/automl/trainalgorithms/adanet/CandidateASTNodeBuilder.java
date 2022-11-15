package de.monticore.mlpipelines.automl.trainalgorithms.adanet;

import de.monticore.lang.monticar.cnnarch._ast.*;
import de.monticore.mlpipelines.automl.trainalgorithms.adanet.builder.ASTLayerBuilderCustom;
import de.monticore.mlpipelines.automl.trainalgorithms.adanet.builder.ASTParallelBlockBuilderCustom;
import de.monticore.mlpipelines.automl.trainalgorithms.adanet.builder.ASTStreamBuilderCustom;

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
                if (layer.getName().equals("Adanet")) {
                    elements.set(i, getCandidate());
                }
            }
        }
    }

    private ASTParallelBlock getCandidate() {
        ASTParallelBlockBuilder builder = new ASTParallelBlockBuilderCustom();
        for (AdaNetComponent component : this.candidate.getAllComponents()) {
            ASTStream stream = getAstStream(component);
            builder.addGroups(stream);
        }
        return builder.build();
    }

    private ASTStream getAstStream(AdaNetComponent component) {
        ASTStreamBuilder streamBuilder = new ASTStreamBuilderCustom();
        for (int i = 0; i < component.getDepth(); i++) {
            ASTLayer layer = getAstLayer(component.getLayerWidth());
            streamBuilder.addElements(layer);
        }
        return streamBuilder.build();
    }

    private ASTLayer getAstLayer(int layerWidth) {
        ASTLayerBuilder layerBuilder = new ASTLayerBuilderCustom();
        layerBuilder.setName("FullyConnected");
        ASTLayer layer = layerBuilder.build();
        return layer;
    }
}
