package de.monticore.mlpipelines.automl.trainalgorithms.adanet;

public class AdaNetComponent {
    private int numberLayers;

    public AdaNetComponent(int numberLayers) {
        this.numberLayers = numberLayers;
    }

    public int getNumberLayers() {
        return this.numberLayers;
    }

    public void setNumberLayers(int numberLayers) {
        this.numberLayers = numberLayers;
    }

}
