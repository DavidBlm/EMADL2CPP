package de.monticore.lang.monticar.emadl.generator.modularcnn;

import de.monticore.lang.embeddedmontiarc.embeddedmontiarc._symboltable.cncModel.EMAComponentSymbol;
import de.monticore.lang.embeddedmontiarc.embeddedmontiarc._symboltable.cncModel.EMAPortArraySymbol;
import de.monticore.lang.embeddedmontiarc.embeddedmontiarc._symboltable.instanceStructure.EMAComponentInstanceSymbol;
import de.monticore.lang.embeddedmontiarc.embeddedmontiarc._symboltable.instanceStructure.EMAPortInstanceSymbol;
import de.monticore.lang.monticar.cnnarch._ast.ASTArchitecture;
import de.monticore.lang.monticar.cnnarch._symboltable.*;
import de.monticore.lang.monticar.emadl.generator.emadlgen.EMADLGenerator;
import de.monticore.lang.monticar.emadl.modularcnn.composer.NetworkStructureInformation;
import de.monticore.lang.tagging._symboltable.TaggingResolver;
import de.monticore.symboltable.CommonScope;
import de.monticore.symboltable.MutableScope;
import de.monticore.symboltable.Scope;
import de.monticore.symboltable.Symbol;
import de.se_rwth.commons.logging.Log;

import java.util.*;

public class NetworkComposer {

    private ComposedNetworkHandler composedNetworkHandler = null;
    private Set<EMAComponentInstanceSymbol> instanceVault = null;
    private LinkedHashMap<String,ArchitectureSymbol> cachedComposedArchitectureSymbols = null;

    public NetworkComposer(ComposedNetworkHandler composedNetworkHandler, LinkedHashMap<String, ArchitectureSymbol> cachedComposedArchitectureSymbols){
        this.composedNetworkHandler = composedNetworkHandler;
        this.cachedComposedArchitectureSymbols = cachedComposedArchitectureSymbols;

    }

    public NetworkComposer(ComposedNetworkHandler composedNetworkHandler, Set<EMAComponentInstanceSymbol> instanceVault, LinkedHashMap<String,ArchitectureSymbol> cachedComposedArchitectureSymbols){
        this.composedNetworkHandler = composedNetworkHandler;
        this.instanceVault = instanceVault;
        this.cachedComposedArchitectureSymbols = cachedComposedArchitectureSymbols;
    }

    public ArchitectureSymbol generateComposedNetwork(NetworkStructureInformation networkStructureInformation, EMAComponentInstanceSymbol fromInstance){
        Log.info("Cached Architecture Symbols: " + cachedComposedArchitectureSymbols.toString(),"NETWORK_COMPOSITION");
        ArchitectureSymbol cachedSymbol = cachedComposedArchitectureSymbols.get(fromInstance.getFullName());
        if (cachedSymbol != null) return cachedSymbol;

        ArchitectureSymbol composedNet = null;
        try {
            composedNet = generateNetworkLevel(networkStructureInformation, fromInstance);
        } catch (Exception e){
            Log.error("Generation of composed network failed");
            e.printStackTrace();
            throw new RuntimeException();
        }

        return composedNet;
    }

    public ArchitectureSymbol generateNetworkLevel(NetworkStructureInformation networkStructureInformation, EMAComponentInstanceSymbol fromInstance) throws Exception {
        Log.info("Generating Network Level","NETWORK_COMPOSITION");
        ArrayList<NetworkStructureInformation> subnets = networkStructureInformation.getSubNetworks();
        ArrayList<ArchitectureSymbol> subnetArchSymbols = new ArrayList<>();
        if (subnets != null && subnets.size() > 0){
            ArrayList<String> dataFlowList = networkStructureInformation.getNetworkInstancesDataFlow();
            ArrayList<String> instanceOnlyDataFlow = new ArrayList<>();
            for (String net : dataFlowList){
                    String[] netSplit  = net.split("\\|");
                    if (netSplit.length == 2){
                        instanceOnlyDataFlow.add(netSplit[1]);
                    }
            }

            for (String dataFlowElement : instanceOnlyDataFlow){
                for (NetworkStructureInformation subnet : subnets){
                    if (!subnet.getInstanceSymbolName().equals(dataFlowElement)) continue;

                    EMAComponentInstanceSymbol fromSubnetInstance = findSubnetInstance(subnet,fromInstance);
                    if (fromSubnetInstance == null) throw new Exception("Could not find subnet instance in instances of symbol");

                    ArchitectureSymbol cachedSubnetArchSymbol = cachedComposedArchitectureSymbols.get(fromSubnetInstance.getFullName());
                    if (cachedSubnetArchSymbol != null){
                        subnetArchSymbols.add(cachedSubnetArchSymbol);
                        break;
                    }

                    if (subnet.isAtomic()) {
                        if ( (subnet.getInstances() == null || subnet.getInstances().size() == 0 )
                        && (instanceVault == null || instanceVault.size() == 0) ) return null;
                        //Optional<ArchitectureSymbol> architectureOpt = subnet.getInstances().get(0).getSpannedScope().resolve("", ArchitectureSymbol.KIND);
                        Optional<ArchitectureSymbol> architectureOpt = fetchSubComponentInstanceArchitectureSymbol(subnet, fromInstance);
                        Log.info("ArchitectureSymbol found:"+ architectureOpt.toString() ,"NETWORK_COMPOSITION");
                        if (!architectureOpt.isPresent()){
                            //return null;
                            throw new Exception("Architecture symbol of atomic network missing");
                        } else{
                            subnet.setComposedNetworkArchitectureSymbol(architectureOpt.get());
                            subnetArchSymbols.add(architectureOpt.get());
                            break;
                        }
                    } else {

                        if (fromSubnetInstance == null){
                            throw new Exception("Could not find Instance Symbol for nested Architecture");
                        }

                        ArchitectureSymbol subnetArchSymbol = generateNetworkLevel(subnet, fromSubnetInstance);
                        if (subnetArchSymbol != null){
                            subnetArchSymbols.add(subnetArchSymbol);
                            break;
                        }
                    }
                }
            }

        }

        ArchitectureSymbol composedNet = mergeArchitectureSymbols(subnetArchSymbols, networkStructureInformation);
        fixScopesOfMergedArchitectureSymbol(composedNet, networkStructureInformation, fromInstance, subnetArchSymbols);

        if (composedNet != null){
            cachedComposedArchitectureSymbols.put(fromInstance.getFullName(), composedNet);
        }

        return composedNet;
    }

    private void fixScopesOfMergedArchitectureSymbol(ArchitectureSymbol composedNet, NetworkStructureInformation networkStructureInformation, EMAComponentInstanceSymbol fromInstance, ArrayList<ArchitectureSymbol> subnetArchSymbols){
        Log.info("FIXING_SCOPES","NETWORK_COMPOSITION");
        if (composedNet == null || subnetArchSymbols.size() < 2) return;

        composedNet.setComponentName(networkStructureInformation.getComponentName());
        for (Scope scope : fromInstance.getEnclosingScope().getSubScopes()){
            CommonScope commonScope = (CommonScope) scope;
            if(commonScope.getSpanningSymbol().get().getFullName().equals(fromInstance.getFullName())){
                composedNet.setEnclosingScope(commonScope);
                break;
            }
        }

        String composedFullName = composedNet.getEnclosingScope().getSpanningSymbol().get().getFullName() + ".";
        ArchitectureSymbol sym = (ArchitectureSymbol) composedNet.getNetworkInstructions().get(0).getEnclosingScope().getSpanningSymbol().get();
        sym.setFullName(composedFullName);

        MutableScope enclosingScope = composedNet.getEnclosingScope().getAsMutableScope();


        Map<String,Collection<Symbol>> composedNetSymbols = composedNet.getEnclosingScope().getLocalSymbols();
        ArrayList<Symbol> toRemove = new ArrayList<>();
        for (String key : composedNetSymbols.keySet() ) {
            ArrayList<Symbol> symbolArrayList = (ArrayList<Symbol>) composedNetSymbols.get(key);
            for(Symbol symbol : symbolArrayList){
                toRemove.add(symbol);
            }
        }

        for (Symbol symbol : toRemove){
            enclosingScope.remove(symbol);
        }



        List<CommonScope> composedNetSubScopes = (List<CommonScope>) composedNet.getEnclosingScope().getAsMutableScope().getSubScopes();
        for (CommonScope subScope: composedNetSubScopes){
            enclosingScope.removeSubScope(subScope);
        }



        for (int i = 0; i < subnetArchSymbols.size(); i++){
            if (i==0){
                composedNet.getSpannedScope().getAsMutableScope().setAstNode(subnetArchSymbols.get(i).getAstNode().get());
                composedNet.setAstNode(subnetArchSymbols.get(i).getAstNode().get());
            }

            Map<String,Collection<Symbol>> symbols = subnetArchSymbols.get(i).getEnclosingScope().getLocalSymbols();
            for (String key : symbols.keySet() ) {
                ArrayList<Symbol> symbolArrayList = (ArrayList<Symbol>) symbols.get(key);
                for(Symbol symbol : symbolArrayList){
                    if (i == 0 ){
                        if (symbol instanceof EMAPortInstanceSymbol && !((EMAPortInstanceSymbol) symbol).isIncoming() ){
                            continue;
                        } else if (symbol instanceof EMAPortArraySymbol && !((EMAPortArraySymbol) symbol).isIncoming()) {
                            continue;
                        }
                    }

                    if (i == subnetArchSymbols.size()-1 ){
                        if (symbol instanceof EMAPortInstanceSymbol && ((EMAPortInstanceSymbol) symbol).isIncoming() ){
                            continue;
                        } else if (symbol instanceof EMAPortArraySymbol && ((EMAPortArraySymbol) symbol).isIncoming()) {
                            continue;
                        }
                    }

                    if (symbol instanceof ArchitectureSymbol){
                        continue;
                    }
                    enclosingScope.add(symbol);
                }
            }

            enclosingScope.add(composedNet);

            List<CommonScope> subScopes = (List<CommonScope>) subnetArchSymbols.get(i).getEnclosingScope().getSubScopes();
            for (CommonScope subScope: subScopes){
                //enclosingScope.addSubScope(subScope);
            }
        }
        Log.info("Fixed Scope", "NETWORK_COMPOSITION");
    }


    private ArchitectureSymbol mergeArchitectureSymbols(ArrayList<ArchitectureSymbol> symbols, NetworkStructureInformation networkStructureInformation) throws Exception {
        Log.info("Merging Architecture Symbols","NETWORK_COMPOSITION");
        if (symbols == null || symbols.size() < 2) {
            throw new Exception("Architecture Symbol Merge error: "  + "Not enough symbols to merge (at least 2 required)");
        }

        ArrayList<ArchitectureSymbol> dataFlowSymbols = symbols;
        ArchitectureSymbol mergedArchitecture = new ArchitectureSymbol();

        ArrayList<ArchitectureSymbol> mergedAuxiliaryArchitecture = new ArrayList<>();
        ArrayList<LayerVariableDeclarationSymbol> mergedLayerVariableDeclarations = new ArrayList<>();
        ArrayList<NetworkInstructionSymbol> mergedNetworkInstructions = new ArrayList<>();
        ArrayList<String> mergedAdaNetUtils = new ArrayList<>();
        ArrayList<String> mergedCustomFilePaths = new ArrayList<>();
        ArrayList<Boolean> mergedUseDgls = new ArrayList<>();
        ArrayList<String> mergedWeightPaths = new ArrayList<>();

        for ( int i=0; i < dataFlowSymbols.size(); i++ ){

            if (dataFlowSymbols.get(i).getAdaNetUtils() != null) mergedAdaNetUtils.add(dataFlowSymbols.get(i).getAdaNetUtils());

            if (dataFlowSymbols.get(i).getCustomPyFilesPath() != null) mergedCustomFilePaths.add(dataFlowSymbols.get(i).getCustomPyFilesPath());
            mergedUseDgls.add(dataFlowSymbols.get(i).getUseDgl());
            if (dataFlowSymbols.get(i).getWeightsPath() != null) mergedWeightPaths.add(dataFlowSymbols.get(i).getWeightsPath());

            if (dataFlowSymbols.get(i).getAuxiliaryArchitecture() != null) mergedAuxiliaryArchitecture.add(dataFlowSymbols.get(i).getAuxiliaryArchitecture());

            if (dataFlowSymbols.get(i).getLayerVariableDeclarations() != null) mergedLayerVariableDeclarations.addAll(dataFlowSymbols.get(i).getLayerVariableDeclarations());


            if (dataFlowSymbols.get(i).getNetworkInstructions() != null) {
                mergedNetworkInstructions.addAll(dataFlowSymbols.get(i).getNetworkInstructions());
            }
        }

        if ( !verifyEqualityString(mergedAdaNetUtils) || !verifyEqualityString(mergedCustomFilePaths) || !verifyEqualityString(mergedWeightPaths)|| !verifyEqualityBoolean(mergedUseDgls)){
            throw new Exception("Architecture Symbol Merge error: "  + "Path(s)/Boolean inequality");
        } else{
            if (mergedAdaNetUtils.size() > 0) mergedArchitecture.setAdaNetUtils(mergedAdaNetUtils.get(0));
            if (mergedCustomFilePaths.size() > 0) mergedArchitecture.setCustomPyFilesPath(mergedCustomFilePaths.get(0));
            if (mergedWeightPaths.size() > 0) mergedArchitecture.setWeightsPath(mergedWeightPaths.get(0));
            if (mergedUseDgls.size() > 0) mergedArchitecture.setUseDgl(mergedUseDgls.get(0));
        }

        if (mergedAuxiliaryArchitecture.size() != 0){
            //TODO: Handle Auxiliary Architecture
            Log.info("Auxiliary Architecture present: " + mergedAuxiliaryArchitecture.toString(),"SYMBOL_MERGE");
        }

        if (mergedNetworkInstructions.size() == 0){
            throw new Exception("Architecture Symbol Merge error: "  + "LayerVariableDeclarations/NetworkInstructions malformed");
        }

        mergedArchitecture.setLayerVariableDeclarations(mergedLayerVariableDeclarations);
        mergedArchitecture.setNetworkInstructions(mergeNetworkInstructionsToSingleInstruction(mergedNetworkInstructions));

        mergedArchitecture.setInputs(dataFlowSymbols.get(0).getInputs());
        mergedArchitecture.setOutputs(dataFlowSymbols.get(dataFlowSymbols.size()-1).getOutputs());



        return mergedArchitecture;
    }



    private ArrayList<NetworkInstructionSymbol> mergeNetworkInstructionsToSingleInstruction(ArrayList<NetworkInstructionSymbol> seperatedInstructions){
        Log.info("Merging Network Instructions to single Instruction","NETWORK_COMPOSITION");
        if (seperatedInstructions.size() < 2) return seperatedInstructions;

        ArrayList<NetworkInstructionSymbol> mergedInstructions = new ArrayList<>();
        NetworkInstructionSymbol targetInstruction = seperatedInstructions.get(0);

        ArrayList<ArchitectureElementSymbol> targetElementSymbols = (ArrayList<ArchitectureElementSymbol>) targetInstruction.getBody().getElements();
        targetElementSymbols.remove(targetElementSymbols.size()-1);

        for (int i=1; i < seperatedInstructions.size(); i++){
            ArrayList<ArchitectureElementSymbol> currentElements = (ArrayList<ArchitectureElementSymbol>) seperatedInstructions.get(i).getBody().getElements();

            if (currentElements.size() > 1){
                currentElements.remove(0);
            }

            if (i != seperatedInstructions.size() - 1 && currentElements.size() > 1){
                currentElements.remove(currentElements.size()-1);
            }

            targetElementSymbols.get(targetElementSymbols.size()-1).setOutputElement(currentElements.get(0));
            currentElements.get(0).setInputElement(targetElementSymbols.get(targetElementSymbols.size()-1));
            targetElementSymbols.addAll(currentElements);
        }

        mergedInstructions.add(targetInstruction);
        return mergedInstructions;
    }

    private Optional<ArchitectureSymbol> fetchSubComponentInstanceArchitectureSymbol(NetworkStructureInformation networkStructureInformation, EMAComponentInstanceSymbol fromInstance){
        Log.info("Fetching subcomponent Architecture Symbol","NETWORK_COMPOSITION");

        EMAComponentInstanceSymbol fromSubnetInstance = findSubnetInstance(networkStructureInformation, fromInstance);
        if (fromSubnetInstance != null){
            return this.composedNetworkHandler.resolveArchitectureSymbolOfInstance(fromSubnetInstance);
        }
        return Optional.empty();
    }

    private EMAComponentInstanceSymbol findSubnetInstance(NetworkStructureInformation subnet, EMAComponentInstanceSymbol fromInstance){
        EMAComponentInstanceSymbol fromSubnetInstance = null;
        Map<String, Collection<Symbol>> symbols = (Map<String, Collection<Symbol>>) fromInstance.getSpannedScope().getLocalSymbols();
        Collection<Symbol> compList = (Collection<Symbol>) symbols.get(subnet.getInstanceSymbolName());
        for (Symbol symbol : compList){
            if (symbol instanceof EMAComponentInstanceSymbol && symbol.getName().equals(subnet.getInstanceSymbolName()) && fromInstance.getFullName().equals(symbol.getPackageName())){
                fromSubnetInstance = (EMAComponentInstanceSymbol) symbol;
                break;
            }
        }
        return fromSubnetInstance;
    }

    private boolean verifyEqualityString(ArrayList<String> list){
        if (list.size() == 0) return true;

        String current = null;
        for (String elem:list){
            if (current == null) current = elem;
            else{
                if (!elem.equals(current)) return false;
            }
        }
        return true;
    }

    private boolean verifyEqualityBoolean(ArrayList<Boolean> list){
        if (list.size() == 0) return true;

        boolean currentSet = false;
        boolean current = false;
        for (Boolean elem: list){
            if (currentSet == false) {
                currentSet = true;
                current = elem;
            }
            else{
                if (current != elem) return false;
            }
        }
        return true;
    }
}
