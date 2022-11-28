package de.monticore.lang.monticar.emadl.generator.modularcnn.decomposers;

import de.monticore.lang.monticar.emadl.generator.modularcnn.networkstructures.ComposedNetworkStructure;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class GluonDecomposer implements BackendDecomposer{

    @Override
    public void decomposeNetwork(String modelPath, ComposedNetworkStructure composedNetworkStructure) {
        ArrayList<File> fileList = scanForFiles(modelPath, composedNetworkStructure);

        File networkJsonFile = null;
        File paramsFile = null;

        File lossNetworkJsonFile = null;
        File lossParamsFile = null;

        for (File file : fileList){
            String fileName = file.getName();
            if (fileName.contains("newest-symbol.json")) networkJsonFile = file;
            else if (fileName.contains("newest-0000.params")) paramsFile = file;
            else if (fileName.contains("loss-symbol.json")) lossNetworkJsonFile = file;
            else if (fileName.contains("loss-0000.params")) lossParamsFile = file;
        }

        splitComposedNetworkIntoAtomicNetworks(modelPath, composedNetworkStructure, networkJsonFile, paramsFile, lossNetworkJsonFile, lossParamsFile);
    }

    /*
    public void decomposeNetworks(String modelPath, HashMap<String, ComposedNetworkStructure> composedNetworkStructures) {
    }*/

    private void splitComposedNetworkIntoAtomicNetworks(String modelPath, ComposedNetworkStructure composedNetworkStructure, File networkFile, File paramsFile, File lossNetworkFile, File lossParamsFile){
        splitNetworkJsonFile(modelPath, composedNetworkStructure, networkFile);
        splitNetworkParamsFile(modelPath, composedNetworkStructure, paramsFile);
        splitLossNetworkJsonFile(modelPath, composedNetworkStructure, lossNetworkFile);
        splitLossNetworkParamsFile(modelPath, composedNetworkStructure, lossParamsFile);
    }

    private void splitNetworkJsonFile(String modelPath, ComposedNetworkStructure composedNetworkStructure, File networkFile){


    }

    private void splitNetworkParamsFile(String modelPath, ComposedNetworkStructure composedNetworkStructure, File paramsFile){

    }

    private void splitLossNetworkJsonFile(String modelPath, ComposedNetworkStructure composedNetworkStructure, File lossNetworkFile){

    }

    private void splitLossNetworkParamsFile(String modelPath, ComposedNetworkStructure composedNetworkStructure, File lossParamsFile){

    }

    private ArrayList<File> scanForFiles(String modelPath, ComposedNetworkStructure composedNetworkStructure){
        ArrayList<File> files = new ArrayList<>();

        String path = modelPath + "/" + composedNetworkStructure.getModelName();
        File modelDirectory = new File(path);
        File[] fileList = modelDirectory.listFiles();

        if (fileList != null){
            for (File file: fileList){
                if (file.isFile()){
                    files.add(file);
                }
            }
        }
        return files;
    }
}
