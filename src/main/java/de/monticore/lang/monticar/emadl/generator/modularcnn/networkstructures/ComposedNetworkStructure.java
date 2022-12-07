package de.monticore.lang.monticar.emadl.generator.modularcnn.networkstructures;

import de.monticore.lang.monticar.cnnarch._symboltable.ArchitectureSymbol;
import de.monticore.lang.monticar.emadl.modularcnn.compositions.NetworkStructureInformation;

import java.util.ArrayList;

public class ComposedNetworkStructure extends NetworkStructure {




    public ComposedNetworkStructure(NetworkStructureInformation networkStructureInformation){
        super(networkStructureInformation);
        this.modelName = null;
    }

    public ComposedNetworkStructure(NetworkStructureInformation networkStructureInformation, ArchitectureSymbol architectureSymbol){
        super(networkStructureInformation, architectureSymbol);
    }






}
