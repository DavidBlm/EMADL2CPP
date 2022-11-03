package de.monticore.lang.monticar.emadl.generator.modularcnn;

import de.monticore.lang.monticar.cnnarch._symboltable.ArchitectureSymbol;
import de.monticore.lang.monticar.cnnarch._symboltable.LayerVariableDeclarationSymbol;
import de.monticore.lang.monticar.cnnarch._symboltable.NetworkInstructionSymbol;
import de.monticore.lang.monticar.cnnarch._symboltable.VariableSymbol;
import de.monticore.lang.monticar.emadl.modularcnn.composer.NetworkStructureInformation;
import de.se_rwth.commons.logging.Log;

import java.util.ArrayList;
import java.util.Optional;

public class NetworkComposer {

    public NetworkComposer(){

    }

    public ArchitectureSymbol generateComposedNetwork(NetworkStructureInformation networkStructureInformation){
        ArchitectureSymbol composedNet = null;
        try {
            composedNet = generateNetworkLevel(networkStructureInformation);
            composedNet.setComponentName(networkStructureInformation.getNetworkName());

        } catch (Exception e){
            Log.error("Generation of composed network failed");
            e.printStackTrace();
        }

        return composedNet;
    }

    public ArchitectureSymbol generateNetworkLevel(NetworkStructureInformation networkStructureInformation) throws Exception {
        ArrayList<NetworkStructureInformation> subnets = networkStructureInformation.getSubNetworks();
        ArrayList<ArchitectureSymbol> subnetArchSymbols = new ArrayList<>();
        if (subnets != null && subnets.size() > 0){

            for (NetworkStructureInformation subnet : subnets){
                if (subnet.isAtomic()) {
                    Optional<ArchitectureSymbol> architectureOpt = subnet.getSymbolReference().getSpannedScope().resolve("", ArchitectureSymbol.KIND);
                    if (!architectureOpt.isPresent()){
                        throw new Exception("Architecture symbol of atomic network missing");
                    }
                    subnetArchSymbols.add(architectureOpt.get());
                } else {
                    subnetArchSymbols.add(generateNetworkLevel(subnet));
                }
            }
        }
        return mergeArchitectureSymbols(subnetArchSymbols);
    }


    private ArchitectureSymbol mergeArchitectureSymbols(ArrayList<ArchitectureSymbol> symbols) throws Exception {

        if (symbols == null || symbols.size() < 2) {
            throw new Exception("Architecture Symbol Merge error: "  + "Not enough symbols to merge (at least 2 required)");
        }

        ArchitectureSymbol mergedArchitecture = new ArchitectureSymbol();

        ArrayList<ArchitectureSymbol> mergedAuxiliaryArchitecture = new ArrayList<>();
        ArrayList<LayerVariableDeclarationSymbol> mergedLayerVariableDeclarations = new ArrayList<>();
        ArrayList<NetworkInstructionSymbol> mergedNetworkInstructions = new ArrayList<>();
        ArrayList<String> mergedAdaNetUtils = new ArrayList<>();
        ArrayList<String> mergedCustomFilePaths = new ArrayList<>();
        ArrayList<Boolean> mergedUseDgls = new ArrayList<>();
        ArrayList<String> mergedWeightPaths = new ArrayList<>();
        ArrayList<VariableSymbol> mergedInputs = new ArrayList<>();
        ArrayList<VariableSymbol> mergedOutputs = new ArrayList<>();

        for (ArchitectureSymbol symbol : symbols){

            mergedAdaNetUtils.add(symbol.getAdaNetUtils());
            mergedCustomFilePaths.add(symbol.getCustomPyFilesPath());
            mergedUseDgls.add(symbol.getUseDgl());
            mergedWeightPaths.add(symbol.getWeightsPath());
            mergedAuxiliaryArchitecture.add(symbol.getAuxiliaryArchitecture());

            mergedLayerVariableDeclarations.addAll(symbol.getLayerVariableDeclarations());
            mergedNetworkInstructions.addAll(symbol.getNetworkInstructions());

            //mergedInputs.addAll(symbol.getInputs());
            //mergedOutputs.addAll(symbol.getOutputs());
        }

        if ( !verifyEqualityString(mergedAdaNetUtils) || !verifyEqualityString(mergedCustomFilePaths) || !verifyEqualityString(mergedWeightPaths)|| !verifyEqualityBoolean(mergedUseDgls)){
            throw new Exception("Architecture Symbol Merge error: "  + "Path(s)/Boolean inequality");
        } else{
            mergedArchitecture.setAdaNetUtils(mergedAdaNetUtils.get(0));
            mergedArchitecture.setCustomPyFilesPath(mergedCustomFilePaths.get(0));
            mergedArchitecture.setWeightsPath(mergedWeightPaths.get(0));
            mergedArchitecture.setUseDgl(mergedUseDgls.get(0));
        }

        if (mergedAuxiliaryArchitecture.size() != 0){
            //TODO: Handle Auxiliary Architecture
            Log.info("Auxiliary Architecture present: " + mergedAuxiliaryArchitecture.toString(),"SYMBOL_MERGE");
        }

        if (mergedLayerVariableDeclarations.size() == 0 || mergedNetworkInstructions.size() == 0){
            throw new Exception("Architecture Symbol Merge error: "  + "LayerVariableDeclarations/NetworkInstructions malformed");
        }

        mergedArchitecture.setLayerVariableDeclarations(mergedLayerVariableDeclarations);
        mergedArchitecture.setNetworkInstructions(mergedNetworkInstructions);

        /*
        if (mergedInputs.size() == 0 || mergedOutputs.size() == 0){
            throw new Exception("Architecture Symbol Merge error: "  + "Inputs and/or Outputs malformed");
        }
        */

        if (symbols.size() < 2){
            throw new Exception("Architecture Symbol Merge error: "  + "Inputs and/or Outputs malformed");
        }

        mergedArchitecture.setInputs(symbols.get(0).getInputs());
        mergedArchitecture.setOutputs(symbols.get(symbols.size()-1).getOutputs());

        return mergedArchitecture;
    }

    private boolean verifyEqualityString(ArrayList<String> list){
        if (list.size() == 0) return false;

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
        if (list.size() == 0) return false;

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
