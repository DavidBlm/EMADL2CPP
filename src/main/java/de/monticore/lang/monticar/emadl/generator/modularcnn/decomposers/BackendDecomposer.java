package de.monticore.lang.monticar.emadl.generator.modularcnn.decomposers;

import de.monticore.lang.monticar.emadl.generator.modularcnn.networkstructures.ComposedNetworkStructure;

import java.util.HashMap;

public interface BackendDecomposer {

    public void decomposeNetwork(String modelPath,  ComposedNetworkStructure composedNetworkStructure);

    //public void decomposeNetworks(String modelPath, HashMap<String, ComposedNetworkStructure> composedNetworkStructures);

}
