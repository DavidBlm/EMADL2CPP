package de.monticore.lang.monticar.emadl.generator.modularcnn;

import de.monticore.lang.embeddedmontiarc.embeddedmontiarc._symboltable.cncModel.EMAComponentSymbol;
import de.monticore.lang.embeddedmontiarc.embeddedmontiarc._symboltable.cncModel.EMAComponentSymbolReference;
import de.monticore.lang.embeddedmontiarc.embeddedmontiarc._symboltable.instanceStructure.EMAComponentInstanceSymbol;
import de.monticore.lang.monticar.cnnarch._symboltable.ArchitectureSymbol;
import de.monticore.lang.monticar.emadl.modularcnn.composer.NetworkStructureInformation;
import de.monticore.lang.monticar.emadl.modularcnn.tools.ComposedNetworkFileHandler;
import de.se_rwth.commons.logging.Log;
import org.checkerframework.checker.units.qual.A;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class ComposedNetworkHandler {
    private String composedNetworkFilePath;
    private ArrayList<NetworkStructureInformation> composedNetworks;
    private NetworkComposer networkComposer;
    private NetworkDecomposer networkDecomposer;
    public ComposedNetworkHandler(String composedNetworkFilePath) {
        this.composedNetworkFilePath = composedNetworkFilePath;
        this.composedNetworks = loadNetworksFromFile(this.composedNetworkFilePath);
        this.networkComposer = new NetworkComposer();
        this.networkDecomposer = new NetworkDecomposer();
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

    public Optional<ArchitectureSymbol> fetchComposedNetworkArchitectureSymbol(EMAComponentInstanceSymbol instanceSymbol){

        for (NetworkStructureInformation networkStructureInformation : composedNetworks){
            if (instanceSymbol != null && networkStructureInformation.getSymbolReference() != null && instanceSymbol.getComponentType().equals(networkStructureInformation.getSymbolReference())) {
                networkStructureInformation.setComposedNetworkArchitectureSymbol(composeNetwork(networkStructureInformation,instanceSymbol));
                ArchitectureSymbol fetchedSymbol = networkStructureInformation.getComposedNetworkArchitectureSymbol();
                if (fetchedSymbol != null){
                    return Optional.of(networkStructureInformation.getComposedNetworkArchitectureSymbol());
                } else {
                    return Optional.empty();
                }

            }
        }
        return Optional.empty();
    }

    public boolean isPartOfComposedNet(EMAComponentInstanceSymbol instanceSymbol){
        for (NetworkStructureInformation networkStructureInformation : composedNetworks){
            if (instanceSymbol != null && networkStructureInformation.isInstancePartOfSubNetworks(instanceSymbol)) {
                return true;
            }
        }
        return false;
    }

    public Optional<ArchitectureSymbol> resolveArchitectureSymbolOfInstance(EMAComponentInstanceSymbol componentInstance){
        if (isComposedNet(componentInstance)){
            return fetchComposedNetworkArchitectureSymbol(componentInstance);
        } else {
            return componentInstance.getSpannedScope().resolve("", ArchitectureSymbol.KIND);
        }
    }

    public Optional<ArchitectureSymbol> resolveArchitectureSymbolOfReferencedSymbol(EMAComponentInstanceSymbol componentInstance){
        if (isComposedNet(componentInstance)){
            return fetchComposedNetworkArchitectureSymbol(componentInstance);
        } else {
            return componentInstance.getComponentType().getReferencedSymbol().getSpannedScope().resolve("", ArchitectureSymbol.KIND);
        }
    }

    public HashSet<EMAComponentInstanceSymbol> filterComposedNetworks(Set<EMAComponentInstanceSymbol> componentInstances){
        if (this.composedNetworks.size() == 0) loadNetworksFromFile(this.composedNetworkFilePath);
        ArrayList<EMAComponentInstanceSymbol> networks = processComponentInstances(componentInstances);
        HashSet<EMAComponentInstanceSymbol> filteredNetworks = new HashSet<>();

        for (EMAComponentInstanceSymbol network: networks){
            if (!isComposedNet(network)){
                filteredNetworks.add(network);
            }
        }
        return filteredNetworks;
    }

    public HashSet<EMAComponentInstanceSymbol> filterAtomicNetworks(Set<EMAComponentInstanceSymbol> componentInstances){
        if (this.composedNetworks.size() == 0) loadNetworksFromFile(this.composedNetworkFilePath);
        ArrayList<EMAComponentInstanceSymbol> networks = processComponentInstances(componentInstances);
        HashSet<EMAComponentInstanceSymbol> filteredNetworks = new HashSet<>();

        for (EMAComponentInstanceSymbol network: networks){
            if (isComposedNet(network)){
                filteredNetworks.add(network);
            }
        }
        return filteredNetworks;
    }

    public HashSet<EMAComponentInstanceSymbol> getSortedNetworksFromAtomicToComposed(Set<EMAComponentInstanceSymbol> componentInstances){
        if (this.composedNetworks.size() == 0) loadNetworksFromFile(this.composedNetworkFilePath);
        HashSet<EMAComponentInstanceSymbol> atomicNetworks = filterComposedNetworks(componentInstances);
        HashSet<EMAComponentInstanceSymbol> composedNetworks = filterAtomicNetworks(componentInstances);
        HashSet<EMAComponentInstanceSymbol> filteredNetworks = new HashSet<>();
        filteredNetworks.addAll(atomicNetworks);
        filteredNetworks.addAll(composedNetworks);

        return filteredNetworks;
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

        for (NetworkStructureInformation networkStructureInformation: this.composedNetworks){
            EMAComponentInstanceSymbol hitInstance = networkStructureInformation.addInstancesAndSymbolReference(componentInstances);
            if (hitInstance != null){
                networks.add(hitInstance);
            }
            //networkStructureInformation.setComposedNetworkArchitectureSymbol(composeNetwork(networkStructureInformation));
        }

        Log.info("Processing instances:" + composedNetworks.toString(),"INSTANCE_PROCESSING");
        return networks;
    }

    private ArrayList<NetworkStructureInformation> loadNetworksFromFile(String composedNetworkFilePath){
        ComposedNetworkFileHandler fileHandler = new ComposedNetworkFileHandler(composedNetworkFilePath);
        return fileHandler.fetchKnownNetworksFromFile();
    }

    public ArchitectureSymbol composeNetwork(NetworkStructureInformation networkStructureInformation,EMAComponentInstanceSymbol fromInstance){
        ArchitectureSymbol composedNetwork = networkComposer.generateComposedNetwork(networkStructureInformation, fromInstance);
        return composedNetwork;
    }

    public ArrayList<EMAComponentInstanceSymbol> refreshInformation(Set<EMAComponentInstanceSymbol> instanceSymbols){
        //this.composedNetworks = loadNetworksFromFile(this.composedNetworkFilePath);
        //ArrayList<EMAComponentInstanceSymbol> refreshed = processComponentInstances(instanceSymbols);
        //return processComponentInstances(instanceSymbols);
        return null;
    }
}
