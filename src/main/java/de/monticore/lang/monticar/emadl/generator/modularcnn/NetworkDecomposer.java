package de.monticore.lang.monticar.emadl.generator.modularcnn;

import de.monticore.lang.monticar.emadl.generator.backend.Backend;
import de.monticore.lang.monticar.emadl.generator.emadlgen.EMADLFileHandler;
import de.monticore.lang.monticar.emadl.generator.modularcnn.decomposers.BackendDecomposer;
import de.monticore.lang.monticar.emadl.generator.modularcnn.decomposers.gluon.GluonDecomposer;
import de.monticore.lang.monticar.emadl.generator.modularcnn.networkstructures.NetworkStructure;

import java.util.HashMap;

public class NetworkDecomposer {

    private Backend backend = null;
    private EMADLFileHandler emadlFileHandler = null;

    public NetworkDecomposer(Backend backend, EMADLFileHandler emadlFileHandler){
        this.backend = backend;
        this.emadlFileHandler = emadlFileHandler;
    }

    public void decomposeNetwork(String modelPath, NetworkStructure composedNetworkStructure){
        BackendDecomposer backendDecomposer = null;
        String backendString = Backend.getBackendString(this.backend);
        switch (backendString){
            case "GLUON":
                backendDecomposer = new GluonDecomposer(emadlFileHandler.getPythonPath());
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
