package de.monticore.mlpipelines.automl.trainalgorithms.adanet;

import de.monticore.lang.monticar.cnnarch._symboltable.ArchitectureSymbol;

import java.util.ArrayList;
import java.util.List;

public class AdaNetComponentFinder {
    public List<AdaNetComponent> findComponents(ArchitectureSymbol startNetwork, int minDepth) {
        List<AdaNetComponent> generatedPossibleComponents = new ArrayList<>();
        int maxDepth = minDepth + 1;
        for (int i = minDepth; i < maxDepth + 1; i++) {
            AdaNetComponent component = new AdaNetComponent(i);
            generatedPossibleComponents.add(component);
        }

        return generatedPossibleComponents;
    }
}
