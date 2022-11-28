package de.monticore.lang.monticar.emadl.generator.modularcnn.networkstructures;

import de.monticore.lang.monticar.cnnarch._symboltable.ArchitectureElementSymbol;
import de.monticore.lang.monticar.cnnarch._symboltable.NetworkInstructionSymbol;

public class LayerInformation {
    private String layerName;
    private LayerType layerType;
    private LayerInformation preceedingLayer = null;
    private LayerInformation succeedingLayer = null;

    private ArchitectureElementSymbol architectureElementSymbol = null;

    public LayerInformation(String layerName, LayerType layerType, ArchitectureElementSymbol architectureElementSymbol){
        this.layerName = layerName;
        this.layerType = layerType;
        this.architectureElementSymbol = architectureElementSymbol;
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
        return this.layerType == LayerType.INPUT && this.preceedingLayer == null && this.succeedingLayer != null;
    }

    public boolean isOutputLayer(){
        return this.layerType == LayerType.OUTPUT && this.preceedingLayer != null && this.succeedingLayer == null;
    }

    public boolean isDefaultLayer(){
        return this.layerType == LayerType.DEFAULT && this.preceedingLayer != null && this.succeedingLayer != null;
    }
}
