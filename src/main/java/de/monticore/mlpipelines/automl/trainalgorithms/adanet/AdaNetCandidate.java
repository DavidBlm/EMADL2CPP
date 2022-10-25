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

    @Override
    public List<String> getEmadl() {
        List<String> emadl = new ArrayList<>();
        return emadl;
    }

    private void addLayerToCandidateContent(List<String> candidateContent, int layerIndex) {
        int numberComponentsInLayer = componentsInLayer(layerIndex);
        if (numberComponentsInLayer > 0) {
            candidateContent.add("FullyConnected(units=layerWidth)->");
        } else {
            candidateContent.add(
                    String.format("FullyConnected(| = %d, units=layerWidth)->", componentsInLayer(layerIndex)));
            candidateContent.add("Concatenate() ->");
        }
    }

    private int componentsInLayer(int layer) {
        int layerDepth = layer + 1;
        int numberComponents = getAllComponents().size();
        for (int componentIndex = layer; componentIndex < numberComponents; componentIndex++) {
            if (componentIsInLayer(layerDepth, componentIndex)) {
                return numberComponents - componentIndex;
            }
        }

        return 0;
    }

    public List<AdaNetComponent> getAllComponents() {
        List<AdaNetComponent> allComponents = new ArrayList<>();
        allComponents.addAll(this.previousComponents);
        allComponents.add(this.component);
        return allComponents;
    }

    private boolean componentIsInLayer(int layerDepth, int componentIndex) {
        return getAllComponents().get(componentIndex).getDepth() >= layerDepth;
    }

    private int getMaxDepth() {
        return component.getDepth();
    }
}
