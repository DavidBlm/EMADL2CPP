package de.monticore.lang.monticar.emadl.generator.modularcnn.networkstructures;

import de.monticore.lang.monticar.emadl.modularcnn.composer.NetworkStructureInformation;
import java.util.ArrayList;

public class AtomicNetworkStructure {

    private ArrayList<LayerInformation> networkLayers = new ArrayList<>();
    private NetworkStructureInformation networkStructureInformation = null;
    private String networkName = null;
    private String instanceSymbolName = null;
    private String componentName = null;
    private LayerInformation frontSlicePoint = null;
    private LayerInformation backSlicePoint = null;



    public AtomicNetworkStructure(NetworkStructureInformation networkStructureInformation){
        this.networkStructureInformation = networkStructureInformation;
        this.networkName = networkStructureInformation.getNetworkName();
        this.instanceSymbolName = networkStructureInformation.getInstanceSymbolName();
        this.componentName = networkStructureInformation.getComponentName();
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
}
