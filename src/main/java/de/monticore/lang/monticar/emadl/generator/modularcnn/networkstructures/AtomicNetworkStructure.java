package de.monticore.lang.monticar.emadl.generator.modularcnn.networkstructures;

import de.monticore.lang.monticar.cnnarch._symboltable.*;
import de.monticore.lang.monticar.emadl.modularcnn.compositions.NetworkStructureInformation;
import java.util.ArrayList;
import java.util.List;

public class AtomicNetworkStructure {

    private ArrayList<LayerInformation> networkLayers = new ArrayList<>();
    private NetworkStructureInformation networkStructureInformation = null;
    private String networkName = null;
    private String instanceSymbolName = null;
    private String componentName = null;
    private LayerInformation frontSlicePoint = null;
    private LayerInformation backSlicePoint = null;



    public AtomicNetworkStructure(NetworkStructureInformation networkStructureInformation, ArchitectureSymbol architectureSymbol){
        this.networkStructureInformation = networkStructureInformation;
        this.networkName = networkStructureInformation.getNetworkName();
        this.instanceSymbolName = networkStructureInformation.getInstanceSymbolName();
        this.componentName = networkStructureInformation.getComponentName();
        processArchitectureSymbol(architectureSymbol);
    }

    public String getNetworkName() {
        return networkName;
    }

    public String getInstanceSymbolName() {
        return instanceSymbolName;
    }

    public String getComponentName() {
        return componentName;
    }

    public ArrayList<LayerInformation> getNetworkLayers(){
        return this.networkLayers;
    }

    public void addNetworkLayer(LayerInformation networkLayer){
        this.networkLayers.add(networkLayer);
    }

    public void addFrontSlicePoint(LayerInformation networkLayer){
        this.networkLayers.add(0,networkLayer);
        this.frontSlicePoint = networkLayer;
    }

    public void addBackSlicePoint(LayerInformation networkLayer){
        this.networkLayers.add(networkLayer);
        this.backSlicePoint = networkLayer;
    }

    private void processArchitectureSymbol(ArchitectureSymbol architectureSymbol){
        List<NetworkInstructionSymbol> networkInstructions = architectureSymbol.getNetworkInstructions();

        for (NetworkInstructionSymbol symbol : networkInstructions){


            List<ArchitectureElementSymbol> elementSymbols = symbol.getBody().getElements();
            for (int i=0; i<elementSymbols.size();i++){
                ArchitectureElementSymbol elementSymbol = elementSymbols.get(i);
                LayerType layerType = null;

                if (elementSymbol instanceof VariableSymbol){
                    if (i==0) layerType = LayerType.INPUT;
                    else layerType = LayerType.OUTPUT;
                } else {
                    LayerSymbol layerSymbol = (LayerSymbol) elementSymbol;
                    if (layerSymbol.getDeclaration().getBody() != null){
                        for (ArchitectureElementSymbol funcSymbol: layerSymbol.getDeclaration().getBody().getElements()) {
                            LayerSymbol funcLayerSymbol = (LayerSymbol) funcSymbol;
                            LayerInformation funcLayer = new LayerInformation(funcLayerSymbol.getName(), LayerType.DEFAULT, funcSymbol);
                            this.addNetworkLayer(funcLayer);
                         }
                        continue;
                        //layerType = LayerType.FUNCTION;
                    } else {
                        layerType = LayerType.DEFAULT;
                    }
                }

                LayerInformation layerInformation = new LayerInformation(elementSymbol.getName(), layerType, elementSymbol);

                if (elementSymbol instanceof VariableSymbol){
                    if (i==0) this.frontSlicePoint = layerInformation;
                    else this.backSlicePoint = layerInformation;
                }
                if (this.networkLayers.size() > 0){
                    this.networkLayers.get(this.networkLayers.size()-1).setSucceedingLayer(layerInformation);
                    layerInformation.setPreceedingLayer(this.networkLayers.get(this.networkLayers.size()-1));
                }


                this.addNetworkLayer(layerInformation);
            }
        }
    }
}
