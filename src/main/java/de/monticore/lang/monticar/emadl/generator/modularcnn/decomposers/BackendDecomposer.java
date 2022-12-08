package de.monticore.lang.monticar.emadl.generator.modularcnn.decomposers;

import de.monticore.lang.monticar.emadl.generator.modularcnn.networkstructures.NetworkStructure;

import java.util.HashMap;

public interface BackendDecomposer {

    public void decomposeNetwork(String modelPath,  NetworkStructure composedNetworkStructure);

    //public void decomposeNetworks(String modelPath, HashMap<String, ComposedNetworkStructure> composedNetworkStructures);

}
