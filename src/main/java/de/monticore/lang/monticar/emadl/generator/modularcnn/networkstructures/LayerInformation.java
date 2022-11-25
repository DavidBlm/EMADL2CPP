package de.monticore.lang.monticar.emadl.generator.modularcnn.networkstructures;

public class LayerInformation {
    private String layerName;
    private LayerType layerType;
    private LayerInformation preceedingLayer = null;
    private LayerInformation succeedingLayer = null;

    public LayerInformation(String layerName, LayerType layerType){
        this.layerName = layerName;
        this.layerType = layerType;
    }

    public LayerInformation getPreceedingLayer() {
        return preceedingLayer;
    }

    public void setPreceedingLayer(LayerInformation preceedingLayer){
        this.preceedingLayer = preceedingLayer;
    }

    public LayerInformation getSucceedingLayer() {
        return succeedingLayer;
    }

    public void setSucceedingLayer(LayerInformation succeedingLayer){
        this.succeedingLayer = succeedingLayer;
    }

    public String getLayerName(){
        return this.layerName;
    }

    public LayerType getLayerType(){
        return this.layerType;
    }

    public boolean isInputLayer(){
        if (this.layerType == LayerType.INPUT && this.preceedingLayer == null && this.succeedingLayer != null) return true;
        return false;
    }

    public boolean isOutputLayer(){
        if (this.layerType == LayerType.OUTPUT && this.preceedingLayer != null && this.succeedingLayer == null) return true;
        return false;
    }

    public boolean isDefaultLayer(){
        if (this.layerType == LayerType.DEFAULT && this.preceedingLayer != null && this.succeedingLayer != null) return true;
        return false;
    }
}
