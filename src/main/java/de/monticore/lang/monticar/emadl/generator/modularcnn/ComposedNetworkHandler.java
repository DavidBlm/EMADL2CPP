package de.monticore.lang.monticar.emadl.generator.modularcnn;

import de.monticore.lang.embeddedmontiarc.embeddedmontiarc._symboltable.cncModel.EMAComponentSymbol;
import de.monticore.lang.embeddedmontiarc.embeddedmontiarc._symboltable.cncModel.EMAComponentSymbolReference;
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

    public String findConfigFileName(EMAComponentInstanceSymbol instanceSymbol){
        if (this.composedNetworks == null) return null;

        for (NetworkStructureInformation networkStructureInformation: composedNetworks){
            if (networkStructureInformation.isInstancePartOfNetwork(instanceSymbol)) {
                return networkStructureInformation.getNetworkName();
            }
        }
        return null;
    }

    //TODO: Finish detection if comp net here
    public boolean isComposedNet(EMAComponentInstanceSymbol instanceSymbol){
        for (NetworkStructureInformation networkStructureInformation : composedNetworks){
            if (instanceSymbol != null && networkStructureInformation.getSymbolReference() != null && instanceSymbol.getComponentType().equals(networkStructureInformation.getSymbolReference())) {
                return true;
            }
        }
        return false;
    }

    public boolean isPartOfComposedNet(EMAComponentInstanceSymbol instanceSymbol){
        for (NetworkStructureInformation networkStructureInformation : composedNetworks){
            if (instanceSymbol != null && networkStructureInformation.isInstancePartOfNetwork(instanceSymbol)) {
                return true;
            }
        }
        return false;
    }


    public ArrayList<EMAComponentInstanceSymbol> processComponentInstances(Set<EMAComponentInstanceSymbol> componentInstances){
        ArrayList<EMAComponentInstanceSymbol> networks = new ArrayList<>();

        if (this.composedNetworks.size() == 0) loadNetworksFromFile(this.composedNetworkFilePath);

        for (EMAComponentInstanceSymbol instanceSymbol: componentInstances){
            EMAComponentSymbol component = instanceSymbol.getComponentType().getReferencedSymbol();
            Optional<ArchitectureSymbol> architecture = component.getSpannedScope().resolve("", ArchitectureSymbol.KIND);
            if (architecture.isPresent()){
                networks.add(instanceSymbol);
            }
        }

        /*
        for (EMAComponentInstanceSymbol instanceSymbol : componentInstances){
            for (NetworkStructureInformation networkStructureInformation: this.composedNetworks){
                String instanceSymbolName = instanceSymbol.getComponentType().getReferencedSymbol().getName();
                if (instanceSymbolName.equals(networkStructureInformation.getNetworkName())){
                    networkStructureInformation.addInstance(instanceSymbol);
                    networkStructureInformation.setSymbolReference((EMAComponentSymbolReference) instanceSymbol.getComponentType());
                    networks.add(instanceSymbol);

                }
            }
        }
        */

        for (NetworkStructureInformation networkStructureInformation: this.composedNetworks){
            EMAComponentInstanceSymbol hitInstance = networkStructureInformation.addInstancesAndSymbolReference(componentInstances);
            if (hitInstance != null){
                networks.add(hitInstance);
            }
        }



        //if (networks.size() == 0) {

        //}

        Log.info("Processing instances:" + composedNetworks.toString(),"INSTANCE_PROCESSING");
        return networks;
    }

    private ArrayList<NetworkStructureInformation> loadNetworksFromFile(String composedNetworkFilePath){
        ComposedNetworkFileHandler fileHandler = new ComposedNetworkFileHandler(composedNetworkFilePath);
        return fileHandler.fetchKnownNetworksFromFile();
    }

    public ArchitectureSymbol ComposeNetwork(NetworkStructureInformation networkStructureInformation){

        return null;
    }
}
