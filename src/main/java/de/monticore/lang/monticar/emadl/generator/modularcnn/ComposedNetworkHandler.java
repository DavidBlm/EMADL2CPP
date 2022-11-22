package de.monticore.lang.monticar.emadl.generator.modularcnn;

import de.monticore.lang.embeddedmontiarc.embeddedmontiarc._symboltable.cncModel.EMAComponentSymbol;
import de.monticore.lang.embeddedmontiarc.embeddedmontiarc._symboltable.instanceStructure.EMAComponentInstanceSymbol;
import de.monticore.lang.monticar.cnnarch._symboltable.ArchitectureSymbol;
import de.monticore.lang.monticar.cnnarch._symboltable.NetworkInstructionSymbol;
import de.monticore.lang.monticar.emadl.generator.emadlgen.EMADLGenerator;
import de.monticore.lang.monticar.emadl.generator.emadlgen.EMADLGeneratorCli;
import de.monticore.lang.monticar.emadl.modularcnn.composer.NetworkStructureInformation;
import de.monticore.lang.monticar.emadl.modularcnn.tools.ComposedNetworkFileHandler;
import de.monticore.lang.tagging._symboltable.TaggingResolver;
import de.se_rwth.commons.logging.Log;

import java.util.*;

public class ComposedNetworkHandler {
    private String composedNetworkFilePath;
    private ArrayList<NetworkStructureInformation> composedNetworks;
    private NetworkComposer networkComposer;
    private NetworkDecomposer networkDecomposer;
    private EMADLGenerator emadlGenerator = null;

    private ArrayList<EMAComponentInstanceSymbol> compositionsToRepeat =  new ArrayList<>();

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
            if (instanceSymbol != null && networkStructureInformation.getSymbolReference() != null
                    //&& instanceSymbol.getComponentType().equals(networkStructureInformation.getSymbolReference())
                    && instanceSymbol.getComponentType().getName().equals(networkStructureInformation.getSymbolReference().getName())
                    && !networkStructureInformation.isAtomic()) {
                return true;
            }
        }
        return false;
    }

    public boolean isComposedNetBySymbolReference(EMAComponentInstanceSymbol instanceSymbol){
        for (NetworkStructureInformation networkStructureInformation : composedNetworks){
            if (instanceSymbol != null && networkStructureInformation.getSymbolReference() != null
                    //&& instanceSymbol.getComponentType().equals(networkStructureInformation.getSymbolReference())
                    && instanceSymbol.getComponentType().getReferencedSymbol().equals(networkStructureInformation.getSymbolReference().getReferencedSymbol())
                    && !networkStructureInformation.isAtomic()) {
                return true;
            }
        }
        return false;
    }

    public Optional<ArchitectureSymbol> fetchComposedNetworkArchitectureSymbol(EMAComponentInstanceSymbol instanceSymbol){

        for (NetworkStructureInformation networkStructureInformation : composedNetworks){
            if (instanceSymbol != null
                    && networkStructureInformation.getSymbolReference() != null
                    //&& instanceSymbol.getComponentType().equals(networkStructureInformation.getSymbolReference())) {
                    //&& instanceSymbol.getComponentType().getName().equals(networkStructureInformation.getSymbolReference().getName())) {
                    && instanceSymbol.getComponentType().getReferencedSymbol().equals(networkStructureInformation.getSymbolReference().getReferencedSymbol())) {
                ArchitectureSymbol fetchedSymbol = composeNetwork(networkStructureInformation,instanceSymbol);
                if (fetchedSymbol != null){
                    return Optional.of(fetchedSymbol);
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
        //if (isComposedNetBySymbolReference(componentInstance)){
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

    /*
    public HashSet<EMAComponentInstanceSymbol> filterForAtomicNetworks(Set<EMAComponentInstanceSymbol> componentInstances){
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

    public HashSet<EMAComponentInstanceSymbol> filterForComposedNetworks(Set<EMAComponentInstanceSymbol> componentInstances){
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

     */

    public HashSet<EMAComponentInstanceSymbol> getSortedNetworksFromAtomicToComposed(Set<EMAComponentInstanceSymbol> componentInstances){
        if (this.composedNetworks.size() == 0) loadNetworksFromFile(this.composedNetworkFilePath);
        /*
        HashSet<EMAComponentInstanceSymbol> atomicNetworks = filterForAtomicNetworks(componentInstances);
        HashSet<EMAComponentInstanceSymbol> composedNetworks = filterForComposedNetworks(componentInstances);
        HashSet<EMAComponentInstanceSymbol> filteredNetworks = new HashSet<>();
        filteredNetworks.addAll(atomicNetworks);
        filteredNetworks.addAll(composedNetworks);
         */

        return new HashSet<>(processComponentInstances(componentInstances));
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
            networkStructureInformation.addInstancesAndSymbolReference(componentInstances);
        }

        for (NetworkStructureInformation networkStructureInformation: this.composedNetworks){
            networks.addAll(networkStructureInformation.getInstances());
        }

        Log.info("Processing instances:" + composedNetworks.toString(),"INSTANCE_PROCESSING");
        return networks;
    }

    private ArrayList<NetworkStructureInformation> loadNetworksFromFile(String composedNetworkFilePath){
        ComposedNetworkFileHandler fileHandler = new ComposedNetworkFileHandler(composedNetworkFilePath);
        return fileHandler.fetchKnownNetworksFromFile();
    }

    public ArchitectureSymbol composeNetwork(NetworkStructureInformation networkStructureInformation, EMAComponentInstanceSymbol fromInstance){
        ArchitectureSymbol composedNetwork = networkComposer.generateComposedNetwork(networkStructureInformation, fromInstance);

        if (composedNetwork == null){
            compositionsToRepeat.add(fromInstance);
        }

        return composedNetwork;
    }

    /*
    public Set<EMAComponentInstanceSymbol> sortComposedNetworksToEnd(Set<EMAComponentInstanceSymbol> instanceSymbols) {
        if (instanceSymbols == null) return instanceSymbols;


        ArrayList<EMAComponentInstanceSymbol> otherComponents = new ArrayList<>();
        ArrayList<EMAComponentInstanceSymbol> composedNets = new ArrayList<>();
        HashSet<EMAComponentInstanceSymbol> sortedComponents = new HashSet<>();

        for (EMAComponentInstanceSymbol instanceSymbol : instanceSymbols) {
            if (isComposedNet(instanceSymbol)) {
                composedNets.add(instanceSymbol);
            } else {
                otherComponents.add(instanceSymbol);
            }
        }

        sortedComponents.addAll(otherComponents);
        sortedComponents.addAll(composedNets);

        return sortedComponents;
    }

    public ArrayList<EMAComponentInstanceSymbol> sortComposedNetworksToEnd(ArrayList<EMAComponentInstanceSymbol> instanceSymbols) {
        if (instanceSymbols == null) return instanceSymbols;


        ArrayList<EMAComponentInstanceSymbol> otherComponents = new ArrayList<>();
        ArrayList<EMAComponentInstanceSymbol> composedNets = new ArrayList<>();
        ArrayList<EMAComponentInstanceSymbol> sortedComponents = new ArrayList<>();

        for (EMAComponentInstanceSymbol instanceSymbol : instanceSymbols) {
            if (isComposedNet(instanceSymbol)) {
                composedNets.add(instanceSymbol);
            } else {
                otherComponents.add(instanceSymbol);
            }
        }

        sortedComponents.addAll(otherComponents);
        sortedComponents.addAll(composedNets);

        return sortedComponents;
    }

    public ArrayList<EMAComponentInstanceSymbol> sortComposedNetworksToEnd(Collection<EMAComponentInstanceSymbol> instanceSymbols) {
        if (instanceSymbols == null) return null;


        ArrayList<EMAComponentInstanceSymbol> otherComponents = new ArrayList<>();
        ArrayList<EMAComponentInstanceSymbol> composedNets = new ArrayList<>();
        ArrayList<EMAComponentInstanceSymbol> sortedComponents = new ArrayList<>();

        for (EMAComponentInstanceSymbol instanceSymbol : instanceSymbols) {
            if (isComposedNet(instanceSymbol)) {
                composedNets.add(instanceSymbol);
            } else {
                otherComponents.add(instanceSymbol);
            }
        }

        sortedComponents.addAll(otherComponents);
        sortedComponents.addAll(composedNets);

        return sortedComponents;
    }
    */

    public ArrayList<EMAComponentInstanceSymbol> getCompositionsToRepeat() {
        return this.compositionsToRepeat;
    }

}
