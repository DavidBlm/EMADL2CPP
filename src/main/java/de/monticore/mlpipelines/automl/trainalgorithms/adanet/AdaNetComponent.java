package de.monticore.mlpipelines.automl.trainalgorithms.adanet;

import de.monticore.mlpipelines.automl.configuration.AdaNetConfig;

import java.util.ArrayList;
import java.util.List;

public class AdaNetComponent implements EmadlAble {
    private int depth;
    private int layerWidth;

    public AdaNetComponent(int depth) {
        this.depth = depth;
        this.layerWidth = AdaNetConfig.UNITS_PER_LAYER;
    }

    public AdaNetComponent(int depth, int layerWidth) {
        this.depth = depth;
        this.layerWidth = layerWidth;
    }

    @Override
    public List<String> getEmadl() {
        List<String> lines = new ArrayList<>();
        int layerWidth = getLayerWidth();

        for (int i = 0; i < getDepth() - 1; i++) {
            String line = String.format("FullyConnected(units=%s) ->", layerWidth);
            lines.add(line);
        }

        String line = String.format("FullyConnected(units=%s)", layerWidth);
        lines.add(line);

        return lines;
    }

    public int getDepth() {
        return this.depth;
    }

    public int getLayerWidth() {
        return this.layerWidth;
    }

    public int setLayerWidth(int layerWidth) {
        return this.layerWidth = layerWidth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }
}
