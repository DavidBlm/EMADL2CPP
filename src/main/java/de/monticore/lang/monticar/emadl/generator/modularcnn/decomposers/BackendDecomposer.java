package de.monticore.lang.monticar.emadl.generator.modularcnn.decomposers;

import de.monticore.lang.monticar.emadl.generator.modularcnn.networkstructures.NetworkStructure;

import java.util.HashMap;

public interface BackendDecomposer {

    void decomposeNetwork(String modelPath, NetworkStructure composedNetworkStructure);

}
