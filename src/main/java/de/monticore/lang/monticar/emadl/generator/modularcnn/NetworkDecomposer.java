package de.monticore.lang.monticar.emadl.generator.modularcnn;

import de.monticore.lang.monticar.emadl.generator.backend.Backend;
import de.monticore.lang.monticar.emadl.generator.emadlgen.FileHandler;
import de.monticore.lang.monticar.emadl.generator.modularcnn.decomposers.BackendDecomposer;
import de.monticore.lang.monticar.emadl.generator.modularcnn.decomposers.gluon.GluonDecomposer;
import de.monticore.lang.monticar.emadl.generator.modularcnn.networkstructures.NetworkStructure;

import java.util.HashMap;

public class NetworkDecomposer {

    private Backend backend = null;
    private FileHandler fileHandler = null;

    public NetworkDecomposer(Backend backend, FileHandler fileHandler){
        this.backend = backend;
        this.fileHandler = fileHandler;
    }

    public void decomposeNetwork(String modelPath, NetworkStructure composedNetworkStructure, String[] decomposeNetworkList){
        BackendDecomposer backendDecomposer = null;

        composedNetworkStructure.setDecompositionControl(decomposeNetworkList);

        String backendString = Backend.getBackendString(this.backend);
        switch (backendString){
            case "GLUON":
                backendDecomposer = new GluonDecomposer(fileHandler.getPythonPath());
                break;
            default:
                backendDecomposer = null;
        }

        if (backendDecomposer == null) {
            throw new RuntimeException("Decomposition for current backend is not supported (yet)");
        }

        backendDecomposer.decomposeNetwork(modelPath, composedNetworkStructure);
    }

    public void decomposeNetworks(String modelPath, HashMap<String, NetworkStructure> composedNetworkStructures, String[] decomposeNetworkList){
        for (String key : composedNetworkStructures.keySet()){
            NetworkStructure composedNetworkStructure = composedNetworkStructures.get(key);
            decomposeNetwork(modelPath, composedNetworkStructure, decomposeNetworkList);
        }
    }



}
