package de.monticore.lang.monticar.emadl.generator.modularcnn;

import de.monticore.lang.embeddedmontiarc.embeddedmontiarc._symboltable.cncModel.EMAComponentSymbol;
import de.monticore.lang.embeddedmontiarc.embeddedmontiarc._symboltable.instanceStructure.EMAComponentInstanceSymbol;
import de.monticore.lang.monticar.cnnarch._symboltable.ArchitectureSymbol;
import de.monticore.lang.monticar.emadl.modularcnn.composer.NetworkStructureInformation;
import de.monticore.lang.monticar.emadl.modularcnn.tools.ComposedNetworkFileHandler;
import de.se_rwth.commons.logging.Log;

import java.util.ArrayList;
import java.util.Optional;
import java.util.Set;

public class ComposedNetworkHandler {
    private String composedNetworkFilePath;
    private ArrayList<NetworkStructureInformation> composedNetworks;
    public ComposedNetworkHandler(String composedNetworkFilePath) {
        this.composedNetworkFilePath = composedNetworkFilePath;
        this.composedNetworks = loadNetworksFromFile(this.composedNetworkFilePath);
    }

    public boolean isComposedNet(String className) {


        return false;
    }

    public void processComponentInstances(Set<EMAComponentInstanceSymbol> componentInstances){
        ArrayList<NetworkStructureInformation> nets = this.composedNetworks;
        if (nets.size() == 0) loadNetworksFromFile(this.composedNetworkFilePath);

        for (EMAComponentInstanceSymbol instanceSymbol: componentInstances){
            EMAComponentSymbol component = instanceSymbol.getComponentType().getReferencedSymbol();
            Optional<ArchitectureSymbol> architecture = component.getSpannedScope().resolve("", ArchitectureSymbol.KIND);
        }



        for (EMAComponentInstanceSymbol instanceSymbol : componentInstances){
            for (NetworkStructureInformation networkStructureInformation: this.composedNetworks){
                String instanceSymbolName = instanceSymbol.getComponentType().getReferencedSymbol().getName();
                if (instanceSymbolName.equals(networkStructureInformation.getNetworkName())){
                    networkStructureInformation.addInstance(instanceSymbol);
                }
            }
        }

        Log.info("Processing instances:" + composedNetworks.toString(),"INSTANCE_PROCESSING");

    }

    private ArrayList<NetworkStructureInformation> loadNetworksFromFile(String composedNetworkFilePath){
        ComposedNetworkFileHandler fileHandler = new ComposedNetworkFileHandler(composedNetworkFilePath);
        return fileHandler.fetchKnownNetworksFromFile();
    }
}
