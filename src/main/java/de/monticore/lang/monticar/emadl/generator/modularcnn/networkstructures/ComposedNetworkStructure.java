package de.monticore.lang.monticar.emadl.generator.modularcnn.networkstructures;

import de.monticore.lang.monticar.emadl.generator.modularcnn.networkstructures.AtomicNetworkStructure;
import de.monticore.lang.monticar.emadl.modularcnn.compositions.NetworkStructureInformation;

import java.util.ArrayList;

public class ComposedNetworkStructure {

    private ArrayList<AtomicNetworkStructure> atomicNetworkStructures = new ArrayList<>();
    private NetworkStructureInformation networkStructureInformation = null;
    private String networkName = null;
    private String instanceSymbolName = null;
    private String componentName = null;
    private String modelName = null;

    public ComposedNetworkStructure(NetworkStructureInformation networkStructureInformation){
        this.networkStructureInformation = networkStructureInformation;
        this.networkName = networkStructureInformation.getNetworkName();
        this.instanceSymbolName = networkStructureInformation.getInstanceSymbolName();
        this.componentName = networkStructureInformation.getComponentName();
        this.modelName = null;
    }

    public String getModelName(){
        return this.modelName;
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

    public NetworkStructureInformation getNetworkStructureInformation() {
        return networkStructureInformation;
    }

    public ArrayList<AtomicNetworkStructure> getAtomicNetworkStructures() {
        return atomicNetworkStructures;
    }

    public void addPrecedingAtomicNetwork(AtomicNetworkStructure atomicNetworkStructure){
        this.atomicNetworkStructures.add(0,atomicNetworkStructure);
    }

    public void addPrecedingAtomicNetworks(ArrayList<AtomicNetworkStructure> atomicNetworkStructuresList){
        this.atomicNetworkStructures.addAll(0,atomicNetworkStructuresList);
    }

    public void addSucceedingAtomicNetwork(AtomicNetworkStructure atomicNetworkStructure){
        this.atomicNetworkStructures.add(atomicNetworkStructure);
    }

    public void addSucceedingAtomicNetworks(ArrayList<AtomicNetworkStructure> atomicNetworkStructuresList){
        this.atomicNetworkStructures.addAll(atomicNetworkStructuresList);
    }


}
