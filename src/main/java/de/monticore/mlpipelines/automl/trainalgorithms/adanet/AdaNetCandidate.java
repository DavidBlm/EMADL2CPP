package de.monticore.mlpipelines.automl.trainalgorithms.adanet;

import java.util.ArrayList;
import java.util.List;

public class AdaNetCandidate implements EmadlAble {
    private AdaNetComponent component;
    private List<AdaNetComponent> previousComponents;

    public AdaNetCandidate() {
    }

    public AdaNetCandidate(AdaNetComponent component, List<AdaNetComponent> previousComponents) {
        this.component = component;
        this.previousComponents = previousComponents;

        if (previousComponents == null) {
            this.previousComponents = new ArrayList<>();
        }
    }

    public AdaNetComponent getComponent() {
        return this.component;
    }

    public void setComponent(AdaNetComponent component) {
        this.component = component;
    }

    public List<AdaNetComponent> getPreviousComponents() {
        return this.previousComponents;
    }

    public void setPreviousComponents(List<AdaNetComponent> previousComponents) {
        this.previousComponents = previousComponents;
    }

    public List<AdaNetComponent> getAllComponents() {
        List<AdaNetComponent> allComponents = new ArrayList<>();
        allComponents.addAll(this.previousComponents);
        allComponents.add(this.component);
        return allComponents;
    }

    @Override
    public List<String> getEmadl() {
        List<String> emadl = new ArrayList<>();
        return emadl;
    }

    public List<String> createCanditateContent(int maxDepth) {
        List<String> candidateContent = new ArrayList<>();
        for (int i = 0; i < maxDepth; i++) {
            candidateContent.add(String.format("        FullyConnected(| = %d, units=layerWidth)->", i + 1));
            candidateContent.add("        Concatenate() ->");
        }
        return candidateContent;
    }

    private int getMaxDepth() {
        return component.getDepth();
    }
}
