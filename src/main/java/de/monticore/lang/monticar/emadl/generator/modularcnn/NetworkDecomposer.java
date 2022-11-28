package de.monticore.lang.monticar.emadl.generator.modularcnn;

import de.monticore.lang.monticar.emadl.generator.backend.Backend;
import de.monticore.lang.monticar.emadl.generator.modularcnn.decomposers.BackendDecomposer;
import de.monticore.lang.monticar.emadl.generator.modularcnn.decomposers.gluon.GluonDecomposer;
import de.monticore.lang.monticar.emadl.generator.modularcnn.networkstructures.ComposedNetworkStructure;

import java.util.HashMap;

public class NetworkDecomposer {

    private Backend backend = null;

    public NetworkDecomposer(Backend backend){
        this.backend = backend;
    }

    public void decomposeNetwork(String modelPath, ComposedNetworkStructure composedNetworkStructure){
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

    public void decomposeNetworks(String modelPath, HashMap<String, ComposedNetworkStructure> composedNetworkStructures){
        for (String key : composedNetworkStructures.keySet()){
            ComposedNetworkStructure composedNetworkStructure = composedNetworkStructures.get(key);
            decomposeNetwork(modelPath,composedNetworkStructure);
        }
    }



}
