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
            //networkStructureInformation.getR
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



        for (EMAComponentInstanceSymbol instanceSymbol : componentInstances){
            for (NetworkStructureInformation networkStructureInformation: this.composedNetworks){
                String instanceSymbolName = instanceSymbol.getComponentType().getReferencedSymbol().getName();
                if (instanceSymbolName.equals(networkStructureInformation.getNetworkName())){
                    networkStructureInformation.addInstance(instanceSymbol);
                    networks.add(instanceSymbol);
                }
            }
        }

        Log.info("Processing instances:" + composedNetworks.toString(),"INSTANCE_PROCESSING");

        return networks;
    }

    private ArrayList<NetworkStructureInformation> loadNetworksFromFile(String composedNetworkFilePath){
        ComposedNetworkFileHandler fileHandler = new ComposedNetworkFileHandler(composedNetworkFilePath);
        return fileHandler.fetchKnownNetworksFromFile();
    }
}
