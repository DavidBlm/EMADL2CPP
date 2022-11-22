package de.monticore.lang.monticar.emadl.generator.modularcnn;

import de.monticore.lang.embeddedmontiarc.embeddedmontiarc._symboltable.cncModel.EMAComponentSymbol;
import de.monticore.lang.embeddedmontiarc.embeddedmontiarc._symboltable.instanceStructure.EMAComponentInstanceSymbol;
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

    private Set<EMAComponentInstanceSymbol> instanceVault = null;
    public NetworkComposer(){

    }

    public NetworkComposer(Set<EMAComponentInstanceSymbol> instanceVault){
        this.instanceVault = instanceVault;
    }

    public ArchitectureSymbol generateComposedNetwork(NetworkStructureInformation networkStructureInformation, EMAComponentInstanceSymbol fromInstance){

        ArchitectureSymbol composedNet = null;
        try {
            composedNet = generateNetworkLevel(networkStructureInformation, fromInstance);
            if (composedNet != null){
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
            }
        } catch (Exception e){
            Log.error("Generation of composed network failed");
            e.printStackTrace();
            throw new RuntimeException();
        }

        return composedNet;
    }

    public ArchitectureSymbol generateNetworkLevel(NetworkStructureInformation networkStructureInformation, EMAComponentInstanceSymbol fromInstance) throws Exception {
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
            //networkStructureInformation.setNetworkInstancesDataFlow(instanceOnlyDataFlow);

            for (String dataFlowElement : instanceOnlyDataFlow){
                for (NetworkStructureInformation subnet : subnets){
                    if (!subnet.getInstanceSymbolName().equals(dataFlowElement)) continue;

                    if (subnet.isAtomic()) {
                        //if (subnet.getInstances() == null || subnet.getInstances().size() == 0) return null;
                        //Optional<ArchitectureSymbol> architectureOpt = subnet.getInstances().get(0).getSpannedScope().resolve("", ArchitectureSymbol.KIND);
                        Optional<ArchitectureSymbol> architectureOpt = fetchSubComponentInstanceArchitectureSymbol(subnet, fromInstance);
                        Log.info("","");
                        if (!architectureOpt.isPresent()){
                            return null;
                            //throw new Exception("Architecture symbol of atomic network missing");
                        } else{
                            subnet.setComposedNetworkArchitectureSymbol(architectureOpt.get());
                            subnetArchSymbols.add(architectureOpt.get());
                        }
                    } else {
                        subnetArchSymbols.add(generateNetworkLevel(subnet, fromInstance));
                    }
                }
            }

        }
        return mergeArchitectureSymbols(subnetArchSymbols, networkStructureInformation);
    }


    private ArchitectureSymbol mergeArchitectureSymbols(ArrayList<ArchitectureSymbol> symbols,NetworkStructureInformation networkStructureInformation) throws Exception {

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

    private ArrayList<NetworkInstructionSymbol> mergeNetworkInstructionsToSingleInstruction(ArrayList<NetworkInstructionSymbol> seperatedInstructions){
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
        Map<String, Collection<Symbol>> symbols = fromInstance.getSpannedScope().getLocalSymbols();
        ArrayList<Symbol> symbolArrayList = (ArrayList<Symbol>)symbols.get(networkStructureInformation.getInstanceSymbolName());

        EMAComponentInstanceSymbol symbol = null;

        for (Symbol sym : symbolArrayList){
            EMAComponentInstanceSymbol instanceSymbol = (EMAComponentInstanceSymbol) sym;
            if (instanceSymbol.getName().equals(networkStructureInformation.getInstanceSymbolName()));{
                symbol = instanceSymbol;
                break;
            }
        }

        if (symbol != null){
            for(EMAComponentInstanceSymbol instanceSymbol : networkStructureInformation.getInstances()){
                if (symbol.getName().equals(instanceSymbol.getName())) return instanceSymbol.getSpannedScope().resolve("", ArchitectureSymbol.KIND);
            }
            if (symbol.getName().equals(networkStructureInformation.getInstanceSymbolName())){
                Log.info("Fetched architecture symbol could be verified by instances of subnetwork (generator did not yet process it probably","NETWORK_COMPOSITION");
                if (instanceVault != null){
                    for (EMAComponentInstanceSymbol instanceSymbol: instanceVault){
                        if (symbol.getName().equals(instanceSymbol.getName())) return instanceSymbol.getSpannedScope().resolve("", ArchitectureSymbol.KIND);
                    }
                }
                //Optional<ArchitectureSymbol> architectureSymbol = symbol.getSpannedScope().resolve("",ArchitectureSymbol.KIND);
                Optional<ArchitectureSymbol> architectureSymbol = Optional.empty();
                return architectureSymbol;
            }
        }
        return Optional.empty();
    }
}
