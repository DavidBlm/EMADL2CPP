package de.monticore.mlpipelines.automl.trainalgorithms.adanet;

import java.util.List;

public class AdaNetComponent {
    private int numberLayers;
    private List<AdaNetComponent> previousComponents;

    public AdaNetComponent(int numberLayers) {
        this.numberLayers = numberLayers;
    }

    public AdaNetComponent(int numberLayers, List<AdaNetComponent> previousComponents) {
        this.numberLayers = numberLayers;
        this.previousComponents = previousComponents;
    }

    public int getNumberLayers() {
        return this.numberLayers;
    }

    public List<AdaNetComponent> getPreviousComponents() {
        return this.previousComponents;
    }
}
