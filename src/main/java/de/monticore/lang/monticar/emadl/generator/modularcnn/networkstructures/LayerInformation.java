package de.monticore.lang.monticar.emadl.generator.modularcnn.networkstructures;

import de.monticore.lang.monticar.cnnarch._symboltable.ArchitectureElementSymbol;
import de.monticore.lang.monticar.cnnarch._symboltable.NetworkInstructionSymbol;

import java.util.ArrayList;

public class LayerInformation {
    private String layerName;
    private ArrayList<String> parallelNames = null;
    private LayerType layerType;
    private LayerInformation preceedingLayer = null;
    private LayerInformation succeedingLayer = null;

    private ArchitectureElementSymbol architectureElementSymbol = null;

    public LayerInformation(String layerName, LayerType layerType, ArchitectureElementSymbol architectureElementSymbol){
        this.layerName = layerName;
        this.layerType = layerType;
        this.architectureElementSymbol = architectureElementSymbol;
    }

    public LayerInformation(ArrayList<String> parallelNames, LayerType layerType, ArchitectureElementSymbol architectureElementSymbol){
        this.layerName = "parallel";
        this.parallelNames = parallelNames;
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

    public ArrayList<String> getParallelNames(){
        return this.parallelNames;
    }

    public boolean parallelNamesContain(String name){
        name = name.replace("_","");
        for (String para : this.parallelNames){
            para = para.replace("_","");
            if (para.equals(name)) return true;
        }
        return false;
    }

    public boolean isParallel() {
        return this.layerType == LayerType.PARALLEL_INPUT || this.layerType == LayerType.PARALLEL_OUTPUT;
    }

    public boolean isInputLayer(){
        return this.layerType == LayerType.INPUT || this.layerType == LayerType.PARALLEL_INPUT; //&& this.preceedingLayer == null && this.succeedingLayer != null;
    }

    public boolean isOutputLayer(){
        return this.layerType == LayerType.OUTPUT || this.layerType == LayerType.PARALLEL_OUTPUT; //&& this.preceedingLayer != null && this.succeedingLayer == null;
    }

    public boolean isDefaultLayer(){
        return this.layerType == LayerType.DEFAULT; //&& this.preceedingLayer != null && this.succeedingLayer != null;
    }

    public boolean isFunctionLayer(){
        return this.layerType == LayerType.FUNCTION; //&& this.preceedingLayer != null && this.succeedingLayer != null;
    }
}
