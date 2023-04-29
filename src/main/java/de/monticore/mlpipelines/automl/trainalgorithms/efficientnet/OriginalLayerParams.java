package de.monticore.mlpipelines.automl.trainalgorithms.efficientnet;

public class OriginalLayerParams {
    private int originalDepthValue;
    private int originalChannelValue;
    private int elementIndex;

    public OriginalLayerParams(int elementIndex, int originalDepthValue, int originalChannelValue) {
        this.elementIndex = elementIndex;
        this.originalDepthValue = originalDepthValue;
        this.originalChannelValue = originalChannelValue;
    }

    public int getOriginalChannelValue() {
        return originalChannelValue;
    }

    public int getOriginalDepthValue() {
        return originalDepthValue;
    }

    public void setOriginalChannelValue(int originalChannelValue) {
        this.originalChannelValue = originalChannelValue;
    }

    public void setOriginalDepthValue(int originalDepthValue) {
        this.originalDepthValue = originalDepthValue;
    }

    public int getElementIndex() {
        return elementIndex;
    }

    public void setElementIndex(int elementIndex) {
        this.elementIndex = elementIndex;
    }
}
