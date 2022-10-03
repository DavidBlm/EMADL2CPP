package de.monticore.lang.monticar.emadl.generator.modularcnn;

import de.monticore.lang.monticar.emadl.modularcnn.composer.NetworkStructureInformation;
import de.monticore.lang.monticar.emadl.modularcnn.tools.ComposedNetworkFileHandler;
import de.se_rwth.commons.logging.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

public class NetworkHandler {
    private String composedNetworkFilePath;
    private ArrayList<NetworkStructureInformation> composedNetworks;
    public NetworkHandler(String composedNetworkFilePath) {
        this.composedNetworkFilePath = composedNetworkFilePath;
        this.composedNetworks = loadNetworksFromFile(this.composedNetworkFilePath);
    }

    public boolean isComposedNet(String className) {


        return false;
    }

    private ArrayList<NetworkStructureInformation> loadNetworksFromFile(String composedNetworkFilePath){
        ComposedNetworkFileHandler fileHandler = new ComposedNetworkFileHandler(composedNetworkFilePath);
        return fileHandler.fetchKnownNetworksFromFile();
    }
}
