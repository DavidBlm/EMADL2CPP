package de.monticore.mlpipelines.automl.trainalgorithms.adanet;

import de.monticore.mlpipelines.automl.configuration.AdaNetConfig;

public class AdaNetComponent {
    private int numberLayers;
    private int layerWidth;

    public AdaNetComponent(int numberLayers) {
        this.numberLayers = numberLayers;
        this.layerWidth = AdaNetConfig.UNITS_PER_LAYER;
    }

    public AdaNetComponent(int numberLayers, int layerWidth) {
        this.numberLayers = numberLayers;
        this.layerWidth = layerWidth;
    }

    public int getNumberLayers() {
        return this.numberLayers;
    }

    public void setNumberLayers(int numberLayers) {
        this.numberLayers = numberLayers;
    }

    public int getLayerWidth() {
        return this.layerWidth;
    }

    public int setLayerWidth(int layerWidth) {
        return this.layerWidth = layerWidth;
    }
}
