package de.monticore.lang.monticar.emadl.generator.modularcnn;

import de.monticore.lang.embeddedmontiarc.embeddedmontiarc._symboltable.instanceStructure.EMAComponentInstanceSymbol;
import de.monticore.lang.monticar.cnnarch._ast.ASTArchitecture;
import de.monticore.lang.monticar.cnnarch._symboltable.*;
import de.monticore.lang.monticar.emadl.modularcnn.composer.NetworkStructureInformation;
import de.monticore.symboltable.CommonScope;
import de.monticore.symboltable.MutableScope;
import de.monticore.symboltable.Scope;
import de.se_rwth.commons.logging.Log;

import java.util.ArrayList;
import java.util.Optional;

public class NetworkComposer {

    public NetworkComposer(){

    }

    public ArchitectureSymbol generateComposedNetwork(NetworkStructureInformation networkStructureInformation,EMAComponentInstanceSymbol fromInstance){

        ArchitectureSymbol composedNet = null;
        try {
            composedNet = generateNetworkLevel(networkStructureInformation);
            if (composedNet != null){
                composedNet.setComponentName(networkStructureInformation.getComponentName());
                for (Scope scope : fromInstance.getEnclosingScope().getSubScopes()){
                    CommonScope commonScope = (CommonScope) scope;
                    if(commonScope.getSpanningSymbol().get().getFullName().equals(fromInstance.getFullName())){
                        composedNet.setEnclosingScope(commonScope);
                        break;
                    }
                }

                //composedNet.setEnclosingScope(fromInstance.get);
                //composedNet.setFullName(fromInstance.getFullName());
            }

            //composedNet.setAstNode(networkStructureInformation.getSubNetworks().get(0).getComposedNetworkArchitectureSymbol().getAstNode().get());
            //composedNet.set
        } catch (Exception e){
            Log.error("Generation of composed network failed");
            e.printStackTrace();
            throw new RuntimeException();
        }

        return composedNet;
    }

    public ArchitectureSymbol generateNetworkLevel(NetworkStructureInformation networkStructureInformation) throws Exception {
        ArrayList<NetworkStructureInformation> subnets = networkStructureInformation.getSubNetworks();
        ArrayList<ArchitectureSymbol> subnetArchSymbols = new ArrayList<>();
        if (subnets != null && subnets.size() > 0){

            for (NetworkStructureInformation subnet : subnets){
                if (subnet.isAtomic()) {
                    if (subnet.getSymbolReference() == null || subnet.getInstances() == null || subnet.getInstances().size() == 0) return null;
                    Optional<ArchitectureSymbol> architectureOpt = subnet.getInstances().get(0).getSpannedScope().resolve("", ArchitectureSymbol.KIND);
                    Log.info("","");
                    if (!architectureOpt.isPresent()){
                        throw new Exception("Architecture symbol of atomic network missing");
                    } else{
                        subnet.setComposedNetworkArchitectureSymbol(architectureOpt.get());
                        subnetArchSymbols.add(architectureOpt.get());
                    }
                } else {
                    subnetArchSymbols.add(generateNetworkLevel(subnet));
                }
            }
        }
        return mergeArchitectureSymbols(subnetArchSymbols, networkStructureInformation);
    }


    private ArchitectureSymbol mergeArchitectureSymbols(ArrayList<ArchitectureSymbol> symbols,NetworkStructureInformation networkStructureInformation) throws Exception {

        if (symbols == null || symbols.size() < 2) {
            throw new Exception("Architecture Symbol Merge error: "  + "Not enough symbols to merge (at least 2 required)");
        }

        ArrayList<String> dataFlow = networkStructureInformation.getNetworkInstancesDataFlow();

        if (dataFlow.size() < 3
                || !dataFlow.get(0).equals(networkStructureInformation.getNetworkName())
                || !dataFlow.get(dataFlow.size()-1).equals(networkStructureInformation.getNetworkName())){
            throw new Exception("Architecture Symbol Merge error: "  + "DataFlow not correctly analyzed/given. Malformed networks probably.");
        } else {
            //dataFlow.remove(0);
            //dataFlow.remove(dataFlow.size()-1);
        }

        ArrayList<ArchitectureSymbol> dataFlowSymbols = new ArrayList<>();

        for (int i = 1; i < dataFlow.size()-1; i++){
            for(ArchitectureSymbol symbol : symbols){
                String[] splitCompName = symbol.getComponentName().split("\\.");
                String symbolNetComponentName = splitCompName[splitCompName.length-1];
                if(dataFlow.get(i).equals(symbolNetComponentName)) dataFlowSymbols.add(symbol);
            }
        }

        ArchitectureSymbol mergedArchitecture = new ArchitectureSymbol();
        //mergedArchitecture.setEnclosingScope(networkStructureInformation.getSymbolReference().getEnclosingScope().getAsMutableScope());

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
                //ArrayList<NetworkInstructionSymbol> instructions = new ArrayList<>();
                //instructions.addAll(symbols.get(i).getNetworkInstructions());
                //mergedNetworkInstructions.addAll(stripIoNetworkInstructions(instructions,i,symbols.size()));
                mergedNetworkInstructions.addAll(dataFlowSymbols.get(i).getNetworkInstructions());
            }

            for (NetworkInstructionSymbol symbol : mergedNetworkInstructions){
                ArchitectureElementSymbol first = symbol.getBody().getElements().get(0);
                ArchitectureElementSymbol last = symbol.getBody().getElements().get(symbol.getBody().getElements().size()-1);
                Log.info("first:" + first.toString(),"NETWORK_INSTRUCTION_MERGER");
                Log.info("last:" + last.toString(),"NETWORK_INSTRUCTION_MERGER");
            }

            //mergedInputs.addAll(symbol.getInputs());
            //mergedOutputs.addAll(symbol.getOutputs());
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

        if (/* mergedLayerVariableDeclarations.size() == 0 || */ mergedNetworkInstructions.size() == 0){
            throw new Exception("Architecture Symbol Merge error: "  + "LayerVariableDeclarations/NetworkInstructions malformed");
        }

        mergedArchitecture.setLayerVariableDeclarations(mergedLayerVariableDeclarations);
        mergedArchitecture.setNetworkInstructions(mergedNetworkInstructions);

        /*
        if (mergedInputs.size() == 0 || mergedOutputs.size() == 0){
            throw new Exception("Architecture Symbol Merge error: "  + "Inputs and/or Outputs malformed");
        }
        */
        /*
        if (symbols.size() < 2){
            throw new Exception("Architecture Symbol Merge error: "  + "Inputs and/or Outputs malformed");
        }
        */

        //
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

    private ArrayList<NetworkInstructionSymbol> stripIoNetworkInstructions(ArrayList<NetworkInstructionSymbol> instructions, int index, int symbolListSize){
        ArrayList<NetworkInstructionSymbol> strippedInstructions = new ArrayList<>();

        if (instructions != null && instructions.size() > 0){
            if (index == 0){
                    instructions.remove(instructions.size()-1);

            } else if (index == symbolListSize-1){
                    instructions.remove(0);
            } else {
                instructions.remove(instructions.size()-1);
                instructions.remove(0);
            }
            strippedInstructions = instructions;
        }

        return strippedInstructions;
    }








}
