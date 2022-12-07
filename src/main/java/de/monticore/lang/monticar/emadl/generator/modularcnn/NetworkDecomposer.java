package de.monticore.lang.monticar.emadl.generator.modularcnn;

import de.monticore.lang.monticar.emadl.generator.backend.Backend;
import de.monticore.lang.monticar.emadl.generator.modularcnn.decomposers.BackendDecomposer;
import de.monticore.lang.monticar.emadl.generator.modularcnn.decomposers.gluon.GluonDecomposer;
import de.monticore.lang.monticar.emadl.generator.modularcnn.networkstructures.ComposedNetworkStructure;
import de.monticore.lang.monticar.emadl.generator.modularcnn.networkstructures.NetworkStructure;

import java.util.HashMap;

public class NetworkDecomposer {

    private Backend backend = null;

    public NetworkDecomposer(Backend backend){
        this.backend = backend;
    }

    public void decomposeNetwork(String modelPath, NetworkStructure composedNetworkStructure){
        BackendDecomposer backendDecomposer = null;
        String backendString = Backend.getBackendString(this.backend);
        switch (backendString){
            case "GLUON":
                backendDecomposer = new GluonDecomposer();
                break;
            default:
                backendDecomposer = null;
        }

        if (backendDecomposer == null) {
            throw new RuntimeException("Decomposition for current backend is not supported (yet)");
        }

        backendDecomposer.decomposeNetwork(modelPath, composedNetworkStructure);
    }

    public void decomposeNetworks(String modelPath, HashMap<String, NetworkStructure> composedNetworkStructures){
        for (String key : composedNetworkStructures.keySet()){
            NetworkStructure composedNetworkStructure = composedNetworkStructures.get(key);
            decomposeNetwork(modelPath, composedNetworkStructure);
        }
    }



}
